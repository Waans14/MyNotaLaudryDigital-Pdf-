package com.millenialzdev.mynotadigital;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private EditText Nama, Alamat, NoHp, tanggalMasuk, tanggalKeluar, jumlahPakaian, Berat;
    private ImageButton ibMasuk, ibKeluar;
    private Button btnCetak;
    private Spinner spJenis;

    private double Jumlah;
    private String Harga, jenisLaundry;
    private SimpleDateFormat dateFormat;

    private final int STORGE_PERMISSION_CODE = 1;

    private int tglMasuk;
    private int blnMasuk;
    private int thnMasuk;

    private int tglKeluar;
    private int blnKeluar;
    private int thnKeluar;

    private String jasaLaundry[] = {"- Pilih Jasa Laundry - "
            ,"Cuci Kering (Rp. 3.000/Kg)"
            ,"Cuci + Lipat (Rp. 4.000/Kg)"
            ,"Cuci Kering + Setrika (Rp. 5.000/Kg)"
            ,"Gorden/Seprai (Rp. 10.000/Kg)"
            ,"Boneka (Rp. 10.000/Kg)"
            ,"Bed Cover (Rp. 20.000/Kg)"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Nama = findViewById(R.id.etNama);
        Alamat = findViewById(R.id.etAlamat);
        NoHp = findViewById(R.id.etnoHp);
        tanggalMasuk = findViewById(R.id.etTanggalMasuk);
        tanggalKeluar = findViewById(R.id.etTanggalKeluar);
        jumlahPakaian = findViewById(R.id.etJumlahPakaian);
        Berat = findViewById(R.id.etBerat);
        ibMasuk = findViewById(R.id.ibTanggalMasuk);
        ibKeluar = findViewById(R.id.ibTanggalKeluar);
        btnCetak = findViewById(R.id.btnCetak);
        spJenis = findViewById(R.id.spJenis);

        //Spinner
        ArrayAdapter adapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, jasaLaundry);

        spJenis.setAdapter(adapter);

        //dateFormat
        dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        ibMasuk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                thnMasuk = calendar.get(Calendar.YEAR);
                blnMasuk = calendar.get(Calendar.MONTH);
                tglMasuk = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog pickerDialog;
                pickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        thnMasuk = year;
                        blnMasuk = month;
                        tglMasuk = dayOfMonth;

                        tanggalMasuk.setText(dateFormat.format(calendar.getTime()));
                    }
                }, thnMasuk, blnMasuk, tglMasuk);
                pickerDialog.setTitle("Tanggal Masuk");
                pickerDialog.show();
            }
        });

        ibKeluar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                thnKeluar = calendar.get(Calendar.YEAR);
                blnKeluar = calendar.get(Calendar.MONTH);
                tglKeluar = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog pickerDialog;
                pickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        thnKeluar = year;
                        blnKeluar = month;
                        tglKeluar = dayOfMonth;

                        tanggalKeluar.setText(tglKeluar + "-" + blnKeluar + "-" + thnKeluar);
                    }
                }, thnKeluar, blnKeluar, tglKeluar);
                pickerDialog.setTitle("Tanggal Keluar");
                pickerDialog.show();
            }
        });

        //izin permission write external storage
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
        } else{
            requestStoragePermissions();
        }

        //Cetak Pdf Dan Hitung Total Bayar
        btnCetak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Hitung();

                try {
                    buatPdf();
                }catch (FileNotFoundException | MalformedURLException e){
                    e.printStackTrace();
                }
            }
        });


    }

    private void buatPdf() throws FileNotFoundException, MalformedURLException {
        String pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        File file = new File(pdfPath, "My Nota Digital.pdf");
        OutputStream outputStream = new FileOutputStream(file);

        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument);

        pdfDocument.setDefaultPageSize(PageSize.A5);

        //Menambahkan Gambar
        Drawable drawable = getDrawable(R.drawable.laundry);
        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bitmapData = stream.toByteArray();

        ImageData data = ImageDataFactory.create(bitmapData);
        Image image = new Image(data);

        document.add(image);

        //
        Paragraph spasi = new Paragraph("").setFontSize(12);
        document.add(spasi);

        //getData
        String NamaP = Nama.getText().toString().trim();
        String AlamatP = Alamat.getText().toString().trim();
        String NoHpP = NoHp.getText().toString().trim();
        String JumlahP = jumlahPakaian.getText().toString().trim();
        String beratP = Berat.getText().toString().trim();

        //kolom

        float col = 280f;
        float lebarKolom[] = {col,col};

        Table tableInfo = new Table(lebarKolom);

        tableInfo.addCell(new Cell().add(new Paragraph("Nama : " + NamaP).setFontSize(12).setTextAlignment(TextAlignment.LEFT)).setBorder(Border.NO_BORDER));
        tableInfo.addCell(new Cell().add(new Paragraph("Tanggal Masuk : " + tglMasuk + "/" + blnMasuk + "/" + thnMasuk).setFontSize(12). setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));

        tableInfo.addCell(new Cell().add(new Paragraph("Alamat : " + AlamatP).setFontSize(12). setTextAlignment(TextAlignment.LEFT)).setBorder(Border.NO_BORDER));
        tableInfo.addCell(new Cell().add(new Paragraph("Tanggal Keluar : " + tglKeluar + "/" + blnKeluar + "/" + thnKeluar).setFontSize(12).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));

        tableInfo.addCell(new Cell().add(new Paragraph("No Hp : " + NoHpP).setFontSize(12). setTextAlignment(TextAlignment.LEFT)).setBorder(Border.NO_BORDER));
        tableInfo.addCell(new Cell().add(new Paragraph("").setFontSize(12).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));

        document.add(tableInfo);

        Paragraph spasi1 = new Paragraph("").setFontSize(12);
        document.add(spasi1);

        float lebarKolom2[] = {300f, col, 240f, col};
        Table tableNota = new Table(lebarKolom2);

        //Judul Kolom
        tableNota.addCell(new Cell(). add(new Paragraph("Jenis Laundry").setFontSize(12).setTextAlignment(TextAlignment.CENTER).setBold()));//kolom pertama
        tableNota.addCell(new Cell(). add(new Paragraph("Harga").setFontSize(12).setTextAlignment(TextAlignment.CENTER).setBold()));//kolom kedua
        tableNota.addCell(new Cell(). add(new Paragraph("Berat").setFontSize(12).setTextAlignment(TextAlignment.CENTER).setBold()));//kolom ketiga
        tableNota.addCell(new Cell(). add(new Paragraph("Total").setFontSize(12).setTextAlignment(TextAlignment.CENTER).setBold()));// kolom keempat

        // Isi Kolom
        tableNota.addCell(new Cell(). add(new Paragraph(jenisLaundry).setFontSize(12).setTextAlignment(TextAlignment.CENTER).setBold()));//kolom pertama
        tableNota.addCell(new Cell(). add(new Paragraph(Harga).setFontSize(12).setTextAlignment(TextAlignment.CENTER).setBold()));//kolom kedua
        tableNota.addCell(new Cell(). add(new Paragraph(beratP).setFontSize(12).setTextAlignment(TextAlignment.CENTER).setBold()));//kolom ketiga
        tableNota.addCell(new Cell(). add(new Paragraph("Rp. " + String.valueOf(Jumlah)).setFontSize(12).setTextAlignment(TextAlignment.CENTER).setBold()));// kolom keempat

        // Jumlah pakaian Kolom
        tableNota.addCell(new Cell(). add(new Paragraph("").setFontSize(12).setTextAlignment(TextAlignment.CENTER).setBold()));//kolom pertama
        tableNota.addCell(new Cell(0, 2). add(new Paragraph("Jumlah Pakaian").setFontSize(12).setTextAlignment(TextAlignment.CENTER).setBold()));//kolom kedua & Ketiga
        tableNota.addCell(new Cell(). add(new Paragraph(JumlahP).setFontSize(12).setTextAlignment(TextAlignment.CENTER).setBold()));//kolom keempat

        document.add(tableNota);

        document.close();
        Toast.makeText(MainActivity.this, "Nota Digital Berhasil Dibuat", Toast.LENGTH_SHORT).show();

    }

    private void Hitung() {

        double berat = Double.valueOf(Berat.getText().toString().trim());

        int Spinner = spJenis.getSelectedItemPosition();

        switch (Spinner){
            case 0 :
                Toast.makeText(MainActivity.this, "Maaf, Silahkan Pilih Jasa Laundry!!", Toast.LENGTH_SHORT).show();
                break;
            case 1 :
                jenisLaundry = "Cuci Kering";
                Harga = "Rp. 3.000/Kg";
                Jumlah = (berat * 3000);
                break;
            case 2 :
                jenisLaundry = "Cuci + Lipat";
                Harga = "Rp. 4.000/Kg";
                Jumlah = (berat * 4000);
                break;
            case 3 :
                jenisLaundry = "Cuci + Setrika";
                Harga = "Rp. 5.000/Kg";
                Jumlah = (berat * 5000);
                break;
            case 4 :
                jenisLaundry = "Gorden/Seprai";
                Harga = "Rp. 10.000/Kg";
                Jumlah = (berat * 10000);
                break;
            case 5 :
                jenisLaundry = "Boneka";
                Harga = "Rp. 10.000/Kg";
                Jumlah = (berat * 10000);
                break;
            case 6 :
                jenisLaundry = "Bed Cover";
                Harga = "Rp. 20.000/Kg";
                Jumlah = (berat * 20000);
                break;
        }
    }

    private void requestStoragePermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(this)
                    .setTitle("Permission Dibutuhkan")
                    .setMessage("Permission Dibutuhkan Untuk Mendownload File")
                    .setPositiveButton("Oke", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},STORGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create()
                    .show();
        }else{
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},STORGE_PERMISSION_CODE);
        }
    }
}
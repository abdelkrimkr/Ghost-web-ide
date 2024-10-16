package Ninja.coder.Ghostemane.code;

import android.Manifest;
import android.os.Build;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.shockwave.pdfium.PdfDocument;
import java.io.File;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import android.net.Uri;
import androidx.core.content.FileProvider;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import android.widget.Toast;
import com.github.barteksc.pdfviewer.scroll.*;
import com.github.barteksc.pdfviewer.listener.*;
import com.github.barteksc.pdfviewer.PDFView;
import java.util.List;
import android.util.Log;

public class PdfviewnewActivity extends BaseCompat {

  public final int REQ_CD_CM = 101;
  private PDFView pdfview;
  private MaterialToolbar mtoolbar;
  protected FloatingActionButton mfab;

  private Intent cm = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
  private File _file_cm;

  @Override
  protected void onCreate(Bundle _savedInstanceState) {
    super.onCreate(_savedInstanceState);
    setContentView(R.layout.pdfviewnew);
    initialize(_savedInstanceState);

    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED
        || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_DENIED
        || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_DENIED) {
      ActivityCompat.requestPermissions(
          this,
          new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
          },
          1000);
    } else {
      initializeLogic();
    }
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == 1000) {
      initializeLogic();
    }
  }

  private void initialize(Bundle _savedInstanceState) {
    pdfview = findViewById(R.id.pdfview);
    mtoolbar = findViewById(R.id.toolbar_pdfview);
    mfab = findViewById(R.id.fab_pdfview);
    mfab.setOnClickListener(c -> setMetaDataPdf());
    setSupportActionBar(mtoolbar);
    _file_cm = FileUtil.createNewPictureFile(getApplicationContext());
    Uri _uri_cm;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      _uri_cm =
          FileProvider.getUriForFile(
              getApplicationContext(),
              getApplicationContext().getPackageName() + ".provider",
              _file_cm);
    } else {
      _uri_cm = Uri.fromFile(_file_cm);
    }
    cm.putExtra(MediaStore.EXTRA_OUTPUT, _uri_cm);
    cm.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
  }

  private void initializeLogic() {

    pdfview
        .fromFile(new File(getIntent().getStringExtra("pdf")))
        .enableSwipe(true)
        .swipeHorizontal(false)
        .enableDoubletap(true)
        .defaultPage(0)
        .enableAnnotationRendering(true)
        .password(null)
        .enableAntialiasing(true)
        .spacing(0)
        .scrollHandle(new DefaultScrollHandle(this, false))
        .onLoad(
            new OnLoadCompleteListener() {
              @Override
              public void loadComplete(int totalpages) {
                SketchwareUtil.showMessage(getApplicationContext(), "");
                var di = new GhostWebMaterialDialog(PdfviewnewActivity.this);
                di.setTitle("Pdf Loadet");
                di.setMessage(
                    "Read in Pdf to : "
                        .concat(Uri.parse(getIntent().getStringExtra("pdf")).getLastPathSegment()));
                di.setIcon(R.drawable.pdfview);
                di.setPositiveButton("ok", (p1, d2) -> {});

                di.show();
              }
            })
        .onPageChange(
            new OnPageChangeListener() {
              @Override
              public void onPageChanged(int page, int totalpages) {
                var pdfFileName = Uri.parse(getIntent().getStringExtra("pdf")).getLastPathSegment();
                var pageNumber = page;
                setTitle(
                    String.format(
                        "%s %s / %s", pdfFileName.replaceAll(".pdf", ""), page + 1, totalpages));
                if (page > 1) {
                  mfab.show();
                } else {
                  mfab.hide();
                }
              }
            })
        .load();

    ColorAndroid12.setToolbarinit(mtoolbar);
  }

  @Deprecated
  public void showMessage(String _s) {
    Toast.makeText(getApplicationContext(), _s, Toast.LENGTH_SHORT).show();
  }

  protected void setMetaDataPdf() {
    PdfDocument.Meta mt = pdfview.getDocumentMeta();
    var builder = new StringBuilder();
    builder.append("title: ").append(mt.getTitle()).append("\n");
    builder.append("author: ").append(mt.getAuthor()).append("\n");
    builder.append("subject: ").append(mt.getSubject()).append("\n");
    builder.append("keywords: ").append(mt.getKeywords()).append("\n");
    builder.append("creator: ").append(mt.getCreator()).append("\n");
    builder.append("producer: ").append(mt.getProducer()).append("\n");
    builder.append("creationDate: ").append(mt.getCreationDate()).append("\n");
    builder.append("modDate: ").append(mt.getModDate()).append("\n");
    builder.append("NumberStart: ").append(pdfview.getCurrentPage()).append("\n");
    builder.append("NumberFul: ").append(pdfview.getPageCount()).append("\n");
    printBookmarksTree(pdfview.getTableOfContents(), "-");
    var dialog = new MaterialAlertDialogBuilder(this);
    dialog.setTitle("Meta Data Pdf");
    dialog.setMessage(builder.toString());
    dialog.setPositiveButton("dismiss", null);
    dialog.show();
  }

  public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
    for (PdfDocument.Bookmark b : tree) {

      Log.e("TAG", String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

      if (b.hasChildren()) {
        printBookmarksTree(b.getChildren(), sep + "-");
      }
    }
  }
}

package org.pentaho.reporting.engine.classic.core.bugs;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.TestCase;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pentaho.reporting.libraries.base.encoder.UnsupportedEncoderException;
import org.pentaho.reporting.libraries.base.util.DebugLog;
import org.pentaho.reporting.libraries.base.util.PngEncoder;

/**
 * Todo: Document me!
 * <p/>
 * Date: 07.03.11
 * Time: 14:57
 *
 * @author Thomas Morgner.
 */
public class Prd3278Test extends TestCase
{
  public Prd3278Test()
  {
  }

  public void testSimpleCreate() throws UnsupportedEncoderException, IOException
  {
    Workbook wb = new XSSFWorkbook();
    final Sheet sheet = wb.createSheet();
    PngEncoder encoder = new PngEncoder();
    encoder.setImage(new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB));
    final byte[] data = encoder.pngEncode();
    final int imgId = wb.addPicture(data, Workbook.PICTURE_TYPE_PNG);
    Drawing patriarch = sheet.createDrawingPatriarch();

    final ClientAnchor anchor = wb.getCreationHelper().createClientAnchor();
    anchor.setDx1(0);
    anchor.setDy1(0);
    anchor.setDx2(0);
    anchor.setDy2(0);
    anchor.setCol1(0);
    anchor.setRow1(0);
    anchor.setCol2(1);
    anchor.setRow2(1);
    anchor.setAnchorType(ClientAnchor.MOVE_DONT_RESIZE); // Move, but don't size

    final Picture picture = patriarch.createPicture(anchor, imgId);
//    final ClientAnchor preferredSize = picture.getPreferredSize();
    DebugLog.logHere();
/*
    FileOutputStream fs = new FileOutputStream("out.zip");
    wb.write(fs);
    fs.close();
    */
  }
}

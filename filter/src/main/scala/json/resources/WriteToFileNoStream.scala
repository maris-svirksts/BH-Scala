package json.resources

import com.norbitltd.spoiwo.model._
import com.norbitltd.spoiwo.model.enums.CellFill
import com.norbitltd.spoiwo.natures.xlsx.Model2XlsxConversions._

import java.io.{BufferedWriter, File, FileWriter}

object WriteToFileNoStream {
  /**
   * @param fileName file identifier.
   * @param lines    data to save.
   *
   *                 https://alvinalexander.com/scala/how-to-write-text-files-in-scala-printwriter-filewriter/
   */
  def writePhysicalFile(fileName: String, lines: Seq[String]): Unit = {
    val file: File           = new File(fileName)
    val bw  : BufferedWriter = new BufferedWriter(new FileWriter(file))

    for (line <- lines) {
      bw.write(line)
      bw.newLine()
    }

    bw.close()
  }

  /**
   * @param data      data to save.
   * @param fileName  file identifier.
   * @param separator defines what kind of data it will be: ", " - CSV, "\t" - TSV.
   */
  def writeTextFile(data: Seq[List[String]], fileName: String, separator: String): Unit = {
    val processedData: Seq[String] = data.map(x => x.mkString(separator))

    writePhysicalFile(fileName, processedData)
  }

  /**
   * @param data     data to save.
   * @param fileName file identifier.
   */
  def writeExcelFile(data: Seq[List[String]], fileName: String, headerData: List[String]): Unit = {
    val headerStyle = CellStyle(fillPattern = CellFill.Solid, fillForegroundColor = Color.AquaMarine, font = Font
    (bold = true))

    val preparedSheet = Sheet(name = "Filtered Results").withRows(Row(style = headerStyle).withCellValues(headerData)
                                                                    +: data.map(x => Row().withCellValues(x)))

    preparedSheet.saveAsXlsx("filter/src/main/results/" + fileName + ".xlsx")
  }
}

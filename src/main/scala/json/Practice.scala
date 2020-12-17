package json

import cats.effect.IO
import com.norbitltd.spoiwo.model._
import com.norbitltd.spoiwo.model.enums.CellFill
import com.norbitltd.spoiwo.natures.xlsx.Model2XlsxConversions._
import fs2.Stream
import io.circe.Json
import io.circe.fs2.{decoder, stringArrayParser, stringStreamParser}
import io.circe.generic.extras.Configuration
import json.resources.ADT.PropertyOwner
import scalaj.http.Http

import java.io.{BufferedWriter, File, FileWriter}

object Practice {
  implicit val config: Configuration = Configuration.default

  /**
   * @return parsed JSON for all accounts.
   *
   *         Parse all owner URL's that are provided through master file.
   */
  def fetchOwnerInfoParsed(): Stream[IO, Json] = {
    // Master URL list for accounts.
    val masterSrc: Stream[IO, String]    = Stream(Http("http://localhost:8080/json/links.json").asString.body)
    val masterStream: Stream[IO, Json]   = masterSrc.through(stringArrayParser)
    val masterStreamCompiled: List[Json] = masterStream.compile.toList.unsafeRunSync()
    val masterList: List[String]         = masterStreamCompiled.flatMap(x => {
      x.hcursor.downField("ownerList").as[List[String]].getOrElse(List())
    })

    /**
     * Read in all known data about all accounts.
     */
    val stringStream: Stream[IO, String] = Stream.emits(masterList).map(x => {
      Http(x).asString.body
    })

    stringStream.through(stringStreamParser)
  }

  /**
   * @return Decoded JSON.
   *
   *         Decode the data that was parsed within fetchOwnerInfoParsed().
   */
  def fetchOwnerInfoDecoded(): Stream[IO, PropertyOwner] = {
    val parsedStream: Stream[IO, Json] = fetchOwnerInfoParsed()

    parsedStream.through(decoder[IO, PropertyOwner])
  }

  /**
   * @param fileName file identifier.
   * @param lines    data to save.
   *
   *                 https://alvinalexander.com/scala/how-to-write-text-files-in-scala-printwriter-filewriter/
   */
  def writePhysicalFile(fileName: String, lines: Seq[String]): Unit = {
    val file: File = new File(fileName)
    val bw: BufferedWriter = new BufferedWriter(new FileWriter(file))

    for (line <- lines) {
      bw.write(line)
      bw.newLine()
    }

    bw.close()
  }

  /**
   * @param value ADT element that corresponds to the type defined below.
   * @return ADT element value.
   *
   *         Shortcode and error protection for filter data fields.
   */
  def getValue(value: Option[List[Option[String]]]): String = {
    value.flatMap(x => x.headOption.flatten).getOrElse("")
  }

  /**
   * @param data      data to save.
   * @param fileName  file identifier.
   * @param separator defines what kind of data it will be: ", " - CSV, "\t" - TSV.
   */
  def writeTextFile(data: Seq[List[String]], fileName: String, separator: String): Unit = {
    val processedData: Seq[String] = data.map(x => x.foldLeft("")((left, right) => left + separator + right))

    writePhysicalFile(fileName, processedData)
  }

  /**
   * @param data     data to save.
   * @param fileName file identifier.
   */
  def writeExcelFile(data: Seq[List[String]], fileName: String, headerData: List[String]): Unit = {
    val headerStyle = CellStyle(fillPattern = CellFill.Solid, fillForegroundColor = Color.AquaMarine, font = Font(bold = true))

    val preparedSheet = Sheet(name = "Filtered Results").withRows(Row(style = headerStyle).withCellValues(headerData) +: data.map(x => Row().withCellValues(x)))

    preparedSheet.saveAsXlsx("src/main/results/" + fileName + ".xlsx")
  }
}

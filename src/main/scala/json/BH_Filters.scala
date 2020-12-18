package json

import cats.effect.{Blocker, ContextShift, IO}
import com.norbitltd.spoiwo.model.{CellStyle, Color, Font, Row, Sheet}
import com.norbitltd.spoiwo.model.enums.CellFill
import com.norbitltd.spoiwo.natures.streaming.xlsx.Model2XlsxConversions.XlsxSheet
import fs2.io.file
import fs2.{Stream, text}
import io.circe.Json
import io.circe.fs2.{decoder, stringStreamParser}
import io.circe.generic.extras.Configuration
import json.resources.ADT.PropertyOwner
import json.resources.HelperFunctions.convertToFileFormat
import scalaj.http.Http

import java.nio.file.Paths

object BH_Filters {
  implicit val config: Configuration = Configuration.default

  /**
   * @return parsed JSON for all accounts.
   *
   *         - Parse all owner URL's that are provided through master file.
   *         - Read in all known data about all accounts.
   */
  def fetchOwnerInfoParsed(): Stream[IO, Json] = {
    // Master URL list for accounts.
    val masterSrc   : Stream[IO, String] = Stream(Http("http://localhost:8080/json/links.txt").asString.body)
    val masterStream: Stream[IO, String] = masterSrc
      .through(text.lines)
      .map(x => Http(x).asString.body)

    masterStream.through(stringStreamParser)
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
   * @param in           data to save.
   * @param fileName     file identifier
   * @param parallelism  how many lines to transform at once.
   * @param separator    defines what kind of data it will be: ", " - CSV, "\t" - TSV.
   * @param contextShift implicit.
   * @return Unit
   */
  def saveToTextFile(in: Stream[IO, List[String]], fileName: String, parallelism: Int, separator: String)(
    implicit contextShift: ContextShift[IO]
  ): Stream[IO, Unit] = {
    Stream.resource(Blocker[IO]).flatMap { blocker =>
      in
        .parEvalMapUnordered(parallelism)(convertToFileFormat(_, separator))
        .through(text.utf8Encode)
        .through(file.writeAll(Paths.get(fileName), blocker))
    }
  }
}

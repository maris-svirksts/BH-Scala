package get

import cats.effect.IO
import fs2.Stream
import get.resources._
import io.circe.Json
import io.circe.fs2._
import io.circe.generic.extras._
import scalaj.http.Http

import java.io._

object Practice {
  implicit val config: Configuration = Configuration.default

  /*
   * Parse all owner URL's that are provided through master file.
   */
  def fetchOwnerInfoParsed(): Stream[IO, Json] = {
    // Master URL list for owners.
    val masterSrc: Stream[IO, String] = Stream( Http("http://127.0.0.1:8080/json/links.json").asString.body )
    val masterStream                  = masterSrc.through(stringArrayParser).compile.toList.unsafeRunSync()
    val masterList: List[String]      = masterStream.headOption.get.hcursor.downField("ownerList").as[List[String]].getOrElse(List())

    // Known data about all property owners.
    val stringStream: Stream[IO, String] = Stream.emits(masterList.map(x => { Http(x).asString.body }))

    stringStream.through(stringStreamParser)
  }

  /*
   * Decode the data that was parsed within fetchOwnerInfoParsed().
   */
  def fetchOwnerInfoDecoded(): Stream[IO, PropertyOwner] = {
    val parsedStream: Stream[IO, Json] = fetchOwnerInfoParsed()

    parsedStream.through(decoder[IO, PropertyOwner])
  }

  // https://alvinalexander.com/scala/how-to-write-text-files-in-scala-printwriter-filewriter/
  def writeFile(filename: String, lines: Seq[String]): Unit = {
    val file = new File(filename)
    val bw   = new BufferedWriter(new FileWriter(file))

    for (line <- lines) {
      bw.write(line)
      bw.newLine()
    }

    bw.close()
  }
}
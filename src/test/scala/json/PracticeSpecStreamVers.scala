package json

import cats.effect._
import fs2.Stream
import io.circe.Json
import json.BH_Filters._
import json.resources.ADT._
import json.resources.WriteToFileNoStream._
import json.resources.HelperFunctions.getValue
import org.scalatest.EitherValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PracticeSpecStreamVers extends AnyWordSpec with Matchers with EitherValues {
  implicit val cs: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.global)

  val ownerInfoParsed : Stream[IO, Json]          = fetchOwnerInfoParsed()
  val ownerInfoDecoded: Stream[IO, PropertyOwner] = fetchOwnerInfoDecoded()

  "Owner ID, parsed, first json file" in {
    val ID: Option[Int] = ownerInfoParsed.head.flatMap(x => {
      Stream.emit(x.hcursor.downField("owner").downField("ID").as[Int].getOrElse(-1))
    }).compile.toList.unsafeRunSync().headOption

    ID must be(Some(100))
  }

  "Owner ID, decoded, first json file" in {
    val ID: Option[Int] = ownerInfoDecoded.head.flatMap(x => Stream.emit(Some(x.owner.ID).getOrElse(-1))).compile
      .toList.unsafeRunSync().headOption

    ID must be(Some(100))
  }

  "Filter to File, decoded, (1 owner)" in {
    val filtered: Stream[IO, PropertyOwner] = ownerInfoDecoded.filter(x => {
      x.owner.ID == 44731
    })

    val lines: Stream[IO, List[String]] = for {
      i <- filtered
    } yield List(i.owner.ID.toString, i.owner.display_name)

    saveToTextFile(lines, "src/main/results/out.txt", 100, ", ").compile.drain.unsafeRunSync()

    filtered.compile.toList.unsafeRunSync().size must be(1)
  }

  "Filter, decoded, (1074 owners), Owners with active properties" in {
    val filtered    = ownerInfoDecoded.filter( x => {
      x.owner.properties.getOrElse(Nil).exists {
        case (_, value) if getValue(value.property_data.property_fields.post_status) == "publish" => true
        case _ => false
      }
    } )

    val lines: Stream[IO, List[String]] = for {
      i          <- filtered
      (_, value) <- Stream.fromIterator[IO](i.owner.properties.getOrElse(Nil).iterator)

      if getValue(value.property_data.property_fields.post_status) == "publish"
      shortCode = value.property_data.property_fields
    } yield List(getValue(shortCode.post_title),
                 value.property_data.property_id.toString,
                 getValue(shortCode.User_Name_BH),
                 getValue(shortCode.User_SurName_BH),
                 getValue(shortCode.E_Mail_BH),
                 getValue(shortCode.country),
                 getValue(shortCode.License_variant),
                 getValue(shortCode.post_status))

    val excelHeaders: List[String] = List("Property Title", "Property ID", "Inquiry To Name", "Inquiry To Surname", "Inquiry To Email", "Country", "License", "Post Status")

    writeExcelFile(lines.compile.toList.unsafeRunSync(), "test_p5", excelHeaders)

    filtered.compile.toList.unsafeRunSync().size must be(1074)
  }
}
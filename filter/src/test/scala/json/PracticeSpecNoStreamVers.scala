package json

import cats.effect._
import fs2.Stream
import io.circe.Json
import io.circe.syntax._
import json.BH_Filters._
import json.resources.ADT._
import json.resources.WriteToFileNoStream._
import json.resources.HelperFunctions.getValue
import org.scalatest.EitherValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PracticeSpecNoStreamVers extends AnyWordSpec with Matchers with EitherValues {
  implicit val cs: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.global)

  val ownerInfoParsed: Stream[IO, Json]           = fetchOwnerInfoParsed()
  val ownerInfoDecoded: Stream[IO, PropertyOwner] = fetchOwnerInfoDecoded()

  val boardParsed: List[Json]           = ownerInfoParsed.compile.toList.unsafeRunSync()
  val boardDecoded: List[PropertyOwner] = ownerInfoDecoded.compile.toList.unsafeRunSync()

  "Owner ID, parsed, first json file" in {
    val ID = boardParsed.headOption.flatMap(x => Some(
      x.hcursor.downField("owner").downField("ID").as[Int]
    )).getOrElse(Left(-1))

    ID must be(Right(100))
  }

  "Owner ID, parsed, second json file" in {
    val ID = boardParsed.tail.headOption.flatMap(x => Some(
      x.hcursor.downField("owner").downField("ID").as[Int]
    )).getOrElse(Left(-1))

    ID must be(Right(1002))
  }

  "Owner ID, decoded, first json file" in {
    val ID: Int = boardDecoded.headOption.flatMap(x => Some(x.owner.ID)).getOrElse(-1)

    ID must be(100)
  }

  "User Meta / nickname" in {
    val nickname = boardParsed.headOption.flatMap(x => Some(
      x.hcursor.downField("owner").downField("user_meta").downField("nickname").as[List[String]])
    ).getOrElse(Left(List("none")))

    nickname must be(Right(List("Hirai")))
  }

  "Property ID" in {
    val properties = boardParsed.tail.headOption.flatMap(x => Some(
      x.hcursor.downField("owner").downField("properties").downField("73611")
      .downField("property_data").downField("property_id").as[Int])
    ).getOrElse(Left(-1))

    properties must be(Right(73611))
  }

  "No Filter (1913 owners)" in {
    boardParsed.size must be(1913)
  }

  "Filter (1 owner)" in {
    val filtered: List[Json] = boardParsed.filter(x => x.hcursor.downField("owner").downField("ID").as[String].getOrElse("-1") == "44731")

    filtered.size must be(1)
  }

  "Filter to File, decoded, (1 owner)" in {
    val filtered: List[PropertyOwner] = boardDecoded.filter(x => { x.owner.ID == 44731 } )

    val lines: Seq[List[String]] = for {
      i <- filtered
    } yield List(i.owner.ID.toString, i.owner.display_name)

    writeTextFile(lines, "filter/src/main/results/test_p1.csv", ", ")

    filtered.size must be(1)
  }

  "Filter to File, decoded, (1610 owners)" in {
    val filtered = boardDecoded.filter( x => { x.owner.ID > 500 } )

    val lines = for {
      i <- filtered
      result = List(i.owner.ID.toString, i.owner.user_email, i.owner.display_name)
    } yield result

    val json = ExportJson(lines).asJson.toString()

    writePhysicalFile("filter/src/main/results/results.json", Seq(json))

    filtered.size must be(1610)
  }

  "Filter, decoded, (1 owner), find Owner for property." in {
    val filtered    = boardDecoded.filter( x => {
      x.owner.properties.getOrElse(Nil).exists {
        case (_, value) if value.property_data.property_id == 31171 => true
        case _                                                      => false
      }
    } )

    val lines: Seq[List[String]] = for {
      i      <- filtered
      (_, value) <- i.owner.properties.getOrElse(Nil)

      if value.property_data.property_id == 31171
    } yield List(i.owner.ID.toString, i.owner.display_name, getValue(value.property_data.property_fields.post_title))

    writeTextFile(lines, "filter/src/main/results/test_p2.csv", ", ")

    filtered.size must be(1)
  }

  "Filter, decoded, (484 owners), published_per_night" in {
    val filtered    = boardDecoded.filter( x => {
      x.owner.properties.getOrElse(Nil).exists {
        case (_, value) if getValue(value.property_data.property_fields.published_per_night).toIntOption.getOrElse(0) >= 300 && getValue(value.property_data.property_fields.published_per_night).toIntOption.getOrElse(0) <= 800 => true
        case _                                                                                                                                                                                                                    => false
      }
    } )

    val lines: Seq[List[String]] = for {
      i          <- filtered
      (_, value) <- i.owner.properties.getOrElse(Nil)

      shortCode = value.property_data.property_fields
      if getValue(shortCode.published_per_night).toIntOption.getOrElse(0) >= 300 && getValue(shortCode.published_per_night).toIntOption.getOrElse(0) <= 800
    } yield List(i.owner.ID.toString,i.owner.display_name,getValue(shortCode.post_title),getValue(shortCode.published_per_night))

    writeTextFile(lines, "filter/src/main/results/test_p3.tsv", "\t")

    filtered.size must be(484)
  }

  "Filter, decoded, (1074 owners), Owners with active properties" in {
    val filtered    = boardDecoded.filter( x => {
      x.owner.properties.getOrElse(Nil).exists {
        case (_, value) if getValue(value.property_data.property_fields.post_status) == "publish" => true
        case _ => false
      }
    } )

    val lines: Seq[List[String]] = for {
      i          <- filtered
      (_, value) <- i.owner.properties.getOrElse(Nil)

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

    writeExcelFile(lines, "test_p4", excelHeaders)

    filtered.size must be(1074)
  }
}
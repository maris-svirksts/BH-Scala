package json

import cats.effect.IO
import fs2.Stream
import io.circe.Json
import io.circe.syntax._
import json.Practice._
import json.resources.ADT._
import org.scalatest.EitherValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PracticeSpec extends AnyWordSpec with Matchers with EitherValues {

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
    val filtered = boardParsed.filter( x => x.hcursor.downField("owner").downField("ID").as[String].getOrElse("-1") == "44731")

    filtered.size must be(1)
  }

  "Filter to File, decoded, (1 owner)" in {
    val filtered = boardDecoded.filter( x => { x.owner.ID == 44731 } )

    val lines: Seq[String] = for {
      i <- filtered
    } yield i.owner.ID.toString + ", " + i.owner.display_name

    writeFile("src/main/results/test_p1.csv", lines: Seq[String])

    filtered.size must be(1)
  }

  "Filter to File, decoded, (1610 owners)" in {
    val filtered = boardDecoded.filter( x => { x.owner.ID > 500 } )

    val lines = for {
      i <- filtered
      result = List(i.owner.ID.toString, i.owner.user_email, i.owner.display_name)
    } yield result

    val json = ExportJson(lines).asJson.toString()

    writeFile("src/main/results/results.json", Seq(json))

    filtered.size must be(1610)
  }

  "Filter, decoded, (1 owner), find Owner for property." in {
    val filtered    = boardDecoded.filter( x => {
      x.owner.properties.getOrElse(Nil).exists(y => {
        y match {
          case (_, value) if value.property_data.property_id == 31171 => true
          case _ => false
        }
      })
    } )

    val lines: Seq[String] = for {
      i      <- filtered
      (_, value) <- i.owner.properties.getOrElse(Nil)

      if value.property_data.property_id == 31171
    } yield i.owner.ID.toString + ", " + i.owner.display_name + ", " + getValue(value.property_data.property_fields.post_title)

    writeFile("src/main/results/test_p2.csv", lines: Seq[String])

    filtered.size must be(1)
  }

  "Filter, decoded, (484 owners), published_per_night" in {
    val filtered    = boardDecoded.filter( x => {
      x.owner.properties.getOrElse(Nil).exists(y => {
        y match {
          case (_, value) if getValue(value.property_data.property_fields.published_per_night).toIntOption.getOrElse(0) >= 300 && getValue(value.property_data.property_fields.published_per_night).toIntOption.getOrElse(0) <= 800 => true
          case _ => false
        }
      })
    } )

    val lines: Seq[String] = for {
      i          <- filtered
      (_, value) <- i.owner.properties.getOrElse(Nil)

      shortCode = value.property_data.property_fields
      if getValue(shortCode.published_per_night).toIntOption.getOrElse(0) >= 300 && getValue(shortCode.published_per_night).toIntOption.getOrElse(0) <= 800
    } yield i.owner.ID + "\t" + i.owner.display_name + "\t" + getValue(shortCode.post_title) + "\t" + getValue(shortCode.published_per_night)

    writeFile("src/main/results/test_p3.tsv", lines: Seq[String])

    filtered.size must be(484)
  }

  "Filter, decoded, (1074 owners), Owners with active properties" in {
    val filtered    = boardDecoded.filter( x => {
      x.owner.properties.getOrElse(Nil).exists {
        case (_, value) if getValue(value.property_data.property_fields.post_status) == "publish" => true
        case _ => false
      }
    } )

    val lines: Seq[String] = for {
      i          <- filtered
      (_, value) <- i.owner.properties.getOrElse(Nil)

      if getValue(value.property_data.property_fields.post_status) == "publish"
      shortCode = value.property_data.property_fields
    } yield getValue(shortCode.post_title) + "\t" +
      value.property_data.property_id + "\t" +
      getValue(shortCode.User_Name_BH) + "\t" +
      getValue(shortCode.User_SurName_BH) + "\t" +
      getValue(shortCode.E_Mail_BH) + "\t" +
      getValue(shortCode.country)  + "\t" +
      getValue(shortCode.License_variant)  + "\t" +
      getValue(shortCode.post_status)

    writeFile("src/main/results/test_p4.tsv", lines: Seq[String])

    filtered.size must be(1074)
  }
}
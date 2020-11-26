import io.circe
import io.circe.parser._
import io.circe.generic.JsonCodec
import io.circe.generic.extras._
import org.scalatest.EitherValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scalaj.http.Http


class PracticeSpec extends AnyWordSpec with Matchers with EitherValues {
  import PracticeSpec._

  "Owner ID" in {
    val ownerInfoOrError = fetchOwnerInfo()
    val board = ownerInfoOrError.getOrElse(fail(ownerInfoOrError.toString))
    val ID = board.owner.ID

    ID must be("44731")
  }

  "User Meta / nickname" in {
    val ownerInfoOrError = fetchOwnerInfo()
    val board = ownerInfoOrError.getOrElse(fail(ownerInfoOrError.toString))
    val nickname = board.owner.user_meta.nickname

    nickname.headOption.getOrElse("none") must be("1925cabin")
  }

  "Property ID" in {
    val ownerInfoOrError = fetchOwnerInfo()
    val board = ownerInfoOrError.getOrElse(fail(ownerInfoOrError.toString))
    val properties = board.owner.properties.values

    properties must be("1925cabin")
  }
}

object PracticeSpec extends {
  implicit val config: Configuration = Configuration.default

  @JsonCodec final case class PropertyOwner(owner: UserData)
  @JsonCodec final case class UserData(
                                        ID: String,
                                        user_login: String,
                                        user_meta: UserMeta,
                                        properties: Map[String, PropertyRecord]
                                      )
  @JsonCodec final case class UserMeta(
                                        nickname: List[String],
                                        first_name: List[String]
                                      )

  @JsonCodec final case class PropertyData(property_id: Int)
  @JsonCodec final case class PropertyRecord(property_data: PropertyData)

  private def fetchOwnerInfo(): Either[circe.Error, PropertyOwner] = {
    val body = Http("https://www.boutique-homes.com/remote_search/data.json").asString.body

    decode[PropertyOwner](body)
  }
}
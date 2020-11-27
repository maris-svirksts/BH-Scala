import io.circe
import io.circe.parser._
import io.circe.generic.JsonCodec
import io.circe.generic.extras._
import org.scalatest.EitherValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scalaj.http.Http


class PracticeSpec extends AnyWordSpec with Matchers with EitherValues {
  import Practice._

  "Owner ID" in {
    val ownerInfoOrError = fetchOwnerInfo()
    val board = ownerInfoOrError.getOrElse(fail(ownerInfoOrError.toString))
    val ID = board.owner.ID

    ID must be("44731")
  }

  "User Meta / nickname" in {
    val ownerInfoOrError = fetchOwnerInfo()
    val board = ownerInfoOrError.getOrElse(fail(ownerInfoOrError.toString))
    val nickname = board.owner.user_meta("nickname")

    nickname.headOption.getOrElse("none") must be("1925cabin")
  }

  "Property ID" in {
    val ownerInfoOrError = fetchOwnerInfo()
    val board = ownerInfoOrError.getOrElse(fail(ownerInfoOrError.toString))
    val properties = board.owner.properties("427541").property_data.property_id

    properties must be(427541)
  }
}

object Practice {
  implicit val config: Configuration = Configuration.default
  type RecordList = List[String]

  @JsonCodec final case class PropertyOwner(owner: UserData)
  @JsonCodec final case class UserData(
                                        ID: String,
                                        user_login: String,
                                        user_pass: String,
                                        user_nicename: String,
                                        user_email: String,
                                        user_url: String,
                                        user_registered: String,
                                        user_activation_key: String,
                                        user_status: String,
                                        display_name: String,
                                        user_meta: Map[String, RecordList],
                                        properties: Map[String, PropertyRecord]
                                      )

  @JsonCodec final case class PropertyRecord(
                                              property_data:     PropertyData,
                                              comment:           Map[String, Map[String, String]],
                                              comment_meta:      Map[String, CommentMetaData],
                                              //calendar_data:     Map[String, CalendarData], TODO: Find why it's broken.
                                              conversation_data: Map[String, ConversationData]
                                            )
  @JsonCodec final case class PropertyData(
                                            property_id: Int,
                                            property_fields: PropertyFields
                                          )
  @JsonCodec final case class PropertyFields(post_title: List[String]) //TODO: Include all fields.
    @JsonCodec final case class CommentData(comment_ID: String) //TODO: Include all fields.
  @JsonCodec final case class CommentMetaData(
                                               city:      RecordList,
                                               country:   RecordList,
                                               rating:    RecordList,
                                               ticket_id: RecordList
                                             )
  @JsonCodec final case class CalendarData(calendar_id: String) //TODO: Include all fields.
  @JsonCodec final case class ConversationData(id: String) //TODO: Include all fields.

  def fetchOwnerInfo(): Either[circe.Error, PropertyOwner] = {
    val body = Http("https://www.boutique-homes.com/remote_search/data.json").asString.body

    decode[PropertyOwner](body)
  }
}
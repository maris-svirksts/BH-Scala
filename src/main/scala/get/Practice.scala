package get

import io.circe
import io.circe.generic.JsonCodec
import io.circe.generic.extras._
import io.circe.parser._
import scalaj.http.Http

object Practice {
  implicit val config: Configuration = Configuration.default

  type RecordList  = List[String]
  type Key         = String
  type StringValue = String

  @JsonCodec final case class PropertyOwner(owner: UserData)
  @JsonCodec final case class UserData(
                                        ID:                  String,
                                        user_login:          String,
                                        user_pass:           String,
                                        user_nicename:       String,
                                        user_email:          String,
                                        user_url:            String,
                                        user_registered:     String,
                                        user_activation_key: String,
                                        user_status:         String,
                                        display_name:        String,
                                        user_meta:           Map[String, RecordList],
                                        properties:          Map[String, PropertyRecord]
                                      )

  @JsonCodec final case class PropertyRecord(
                                              property_data:     PropertyData,
                                              comment:           Map[Key, Map[Key, StringValue]],
                                              comment_meta:      Map[Key, CommentMetaData],
                                              //calendar_data:     Map[String, CalendarData], TODO: Find why it's broken.
                                              conversation_data: Map[Key, ConversationData]
                                            )
  @JsonCodec final case class PropertyData(
                                            property_id: Int,
                                            property_fields: PropertyFields
                                          )
  @JsonCodec final case class PropertyFields(post_title: RecordList) //TODO: Include all fields.
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
package ru.fintech.school.ubilling.domain

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalQueries
import java.util.UUID

import spray.json._
import DefaultJsonProtocol._

case class BillResponse(id: UUID, bill: BillView)
object BillResponse {
  implicit lazy val localDateFormat: JsonFormat[LocalDate] = new JsonFormat[LocalDate] {
    override def write(obj: LocalDate): JsValue = JsString(DateTimeFormatter.ISO_DATE.format(obj))

    override def read(json: JsValue): LocalDate = json match {
      case JsString(value) => DateTimeFormatter.ISO_DATE.parse(value, TemporalQueries.localDate())
      case other => deserializationError(s"Expected JsString, got $other")
    }
  }
  implicit lazy val uuidFormat: JsonFormat[UUID] = new JsonFormat[UUID] {
    override def write(obj: UUID): JsValue = JsString(obj.toString)

    override def read(json: JsValue): UUID = json match {
      case JsString(value) => UUID.fromString(value)
      case other => deserializationError(s"Expected JsString, got $other")
    }
  }
  implicit lazy val billItemFormat: RootJsonFormat[BillViewItem] = jsonFormat4(BillViewItem)
  implicit lazy val billFormat: RootJsonFormat[BillView] = jsonFormat3(BillView)
  implicit lazy val billResponseEncoder: RootJsonFormat[BillResponse] = jsonFormat2(BillResponse.apply)
}

case class BillView(name: String, items: Seq[BillViewItem], date: LocalDate)

case class BillViewItem(name: String, description: Option[String], price: BigDecimal, count: BigDecimal)
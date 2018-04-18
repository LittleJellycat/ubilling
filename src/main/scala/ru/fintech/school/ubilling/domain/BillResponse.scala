package ru.fintech.school.ubilling.domain

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalQueries
import java.util.UUID

import spray.json._
import DefaultJsonProtocol._

case class BillResponse(id: UUID, bill: Bill)
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
  implicit lazy val billItemFormat: RootJsonFormat[BillItem] = jsonFormat4(BillItem)
  implicit lazy val billFormat: RootJsonFormat[Bill] = jsonFormat3(Bill)
  implicit lazy val billResponseEncoder: RootJsonFormat[BillResponse] = jsonFormat2(BillResponse.apply)
}

case class Bill(name: String, items: Seq[BillItem], date: LocalDate)

case class BillItem(name: String, description: Option[String], price: BigDecimal, count: BigDecimal)
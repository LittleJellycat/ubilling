package ru.fintech.school.ubilling.schema

import java.sql.Timestamp
import java.util.UUID

import ru.fintech.school.ubilling.HasDbConfigProvider
import slick.jdbc.JdbcProfile

object TableDefinitions {
  type BillId = UUID
  type BillItemId = UUID

  case class Bill(id: BillId, name: String, date: Timestamp)

  case class BillItem(
    iid: BillItemId,
    name: String, description: Option[String],
    price: BigDecimal, count: BigDecimal,
    bid: BillId
  )

  trait BillsTable {
    self: HasDbConfigProvider =>

    import profile.api._

    class Bills(tag: Tag) extends Table[Bill](tag, "BILLS") {
      def bid = column[BillId]("BID", O.PrimaryKey)

      def name = column[String]("BILL_NAME")

      def date = column[Timestamp]("BILL_DATE")

      def * = (bid, name, date) <> (Bill.tupled, Bill.unapply)
    }

    val bills = TableQuery[Bills]
  }

  trait BillItemsTable extends BillsTable {
    self: HasDbConfigProvider =>

    import profile.api._

    class BillItems(tag: Tag) extends Table[BillItem](tag, "BILL_ITEMS") {
      def iid = column[BillItemId]("IID", O.PrimaryKey)

      def name = column[String]("ITEM_NAME")

      def description = column[String]("ITEM_DESCRIPTION")

      def price = column[BigDecimal]("ITEM_PRICE")

      def count = column[BigDecimal]("ITEMS_COUNT")

      def bid = column[BillId]("BILL_ID")

      def fk = foreignKey("fk_bid", bid, bills)(_.bid, onUpdate = ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

      def * = (iid, name, description.?, price, count, bid) <> (BillItem.tupled, BillItem.unapply)
    }

    val billItems = TableQuery[BillItems]
  }

}

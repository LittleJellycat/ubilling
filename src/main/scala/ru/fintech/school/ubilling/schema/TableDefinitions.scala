package ru.fintech.school.ubilling.schema

import java.sql.Timestamp
import java.util.UUID

import ru.fintech.school.ubilling.HasDbConfigProvider

object TableDefinitions {
  type BillId = UUID
  type BillItemId = UUID
  type ImageId = UUID
  type UserId = Long

  case class User(
    uid: UserId,
    email: String,
    phone: Option[String]
  )

  case class Bill(id: BillId, name: String, date: Timestamp)

  case class BillItem(
    iid: BillItemId,
    name: String, description: Option[String],
    price: BigDecimal, count: BigDecimal,
    bid: BillId
  )

  case class BillPhoto(
    bid: BillId,
    photoId: ImageId,
    lastUpdated: Timestamp,
    data: Array[Byte]
  )

  case class UserBill(
    uid: UserId,
    bid: BillId
  )

  trait UsersTable {
    self: HasDbConfigProvider =>

    import profile.api._

    class Users(tag: Tag) extends Table[User](tag, "USERS") {
      def uid = column[UserId]("UID", O.PrimaryKey, O.AutoInc)

      def email = column[String]("EMAIL", O.Unique)

      def phone = column[Option[String]]("PHONE")

      def * = (uid, email, phone) <> (User.tupled, User.unapply)
    }

    val users: TableQuery[Users] = TableQuery[Users]
  }

  trait BillsTable {
    self: HasDbConfigProvider =>

    import profile.api._

    class Bills(tag: Tag) extends Table[Bill](tag, "BILLS") {
      def bid = column[BillId]("BID", O.PrimaryKey)

      def name = column[String]("BILL_NAME")

      def date = column[Timestamp]("BILL_DATE")

      def * = (bid, name, date) <> (Bill.tupled, Bill.unapply)
    }

    val bills: TableQuery[Bills] = TableQuery[Bills]
  }

  trait UsersBillsTable extends BillsTable with UsersTable {
    self: HasDbConfigProvider =>

    import profile.api._

    class UsersBills(tag: Tag) extends Table[UserBill](tag, "USERS_BILLS") {
      def uid = column[UserId]("UID")

      def bid = column[BillId]("BID")

      def pk_constraint = primaryKey("UB_PK", (uid, bid))

      def user_fk_constraint = foreignKey("FK_UID", uid, users)(_.uid, onDelete = ForeignKeyAction.Cascade)

      def bill_fk_constraint = foreignKey("FK_BID", bid, bills)(_.bid, onDelete = ForeignKeyAction.Cascade)

      def * = (uid, bid) <> (UserBill.tupled, UserBill.unapply)
    }

    def usersBills: TableQuery[UsersBills] = TableQuery[UsersBills]

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

      def bid = column[BillId]("BID")

      def fk = foreignKey("FK_ITEMS_BID", bid, bills)(_.bid, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

      def * = (iid, name, description.?, price, count, bid) <> (BillItem.tupled, BillItem.unapply)
    }

    val billItems: TableQuery[BillItems] = TableQuery[BillItems]
  }

  trait BillsPhotosTable {
    self: HasDbConfigProvider =>

    import profile.api._

    class BillsPhotos(tag: Tag) extends Table[BillPhoto](tag, "BILL_PHOTOS") {
      def bid = column[BillId]("BID", O.PrimaryKey)

      def imgId = column[ImageId]("IMG_ID")

      def lastUpdated = column[Timestamp]("LAST_UPDATED")

      def data = column[Array[Byte]]("IMG")

      override def * = (bid, imgId, lastUpdated, data) <> (BillPhoto.tupled, BillPhoto.unapply)
    }

    val billsPhotos: TableQuery[BillsPhotos] = TableQuery[BillsPhotos]
  }
}

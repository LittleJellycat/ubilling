package ru.fintech.school.ubilling.dao

import ru.fintech.school.ubilling.HasDbConfigProvider
import ru.fintech.school.ubilling.schema.TableDefinitions._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

trait UserDao {
  def addUser(user: User): Future[Either[Throwable, UserId]]

  def findUser(userId: UserId): Future[Option[User]]

  def findUserByEmail(mail: String): Future[Option[User]]
}

trait UserBillDao {
  def findBillIds(userId: UserId): Future[Seq[BillId]]

  def assignBill(userId: UserId, billId: BillId): Future[Boolean]

  def findUserIds(billId: BillId): Future[Seq[UserId]]
}

trait RelationalUserDao extends UserDao
  with HasDbConfigProvider
  with UsersTable {

  import profile.api._

  override def addUser(user: User): Future[Either[Throwable, UserId]] = {
    val res = users returning users.map(_.uid) += user
    db.run(res.asTry).map(_.toEither)
  }

  override def findUser(userId: UserId): Future[Option[User]] = {
    db.run(users.filter(_.uid === userId).result.headOption)
  }

  override def findUserByEmail(mail: String): Future[Option[User]] = {
    db.run(users.filter(_.email === mail).result.headOption)
  }
}

trait RelationalUserBillDao extends UserBillDao
  with RelationalUserDao
  with HasDbConfigProvider
  with UsersBillsTable {

  import profile.api._

  override def findBillIds(userId: UserId): Future[Seq[BillId]] = {
    val usersBillsJoin = for {
      (_, b) <- usersBills.filter(_.uid === userId) join bills on (_.bid === _.bid)
    } yield b.bid
    db.run(usersBillsJoin.result)
  }

  override def assignBill(userId: UserId, billId: BillId): Future[Boolean] = {
    db.run(usersBills += UserBill(userId, billId)).map(_ == 1)
  }

  override def findUserIds(billId: BillId): Future[Seq[UserId]] = {
    val usersBillsJoin = for {
      (u, _) <- usersBills.filter(_.bid === billId) join users on (_.uid === _.uid)
    } yield u.uid
    db.run(usersBillsJoin.result)
  }
}

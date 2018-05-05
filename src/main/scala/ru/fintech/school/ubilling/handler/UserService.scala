package ru.fintech.school.ubilling.handler

import ru.fintech.school.ubilling.dao.UserDao
import ru.fintech.school.ubilling.schema.TableDefinitions.{User, UserId}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

trait UserService {
  def addUser(email: String, phone: Option[String]): Future[Option[UserId]]

  def findUser(userId: UserId): Future[Option[User]]
}

class UserServiceImpl(dao: UserDao) extends UserService {
  override def addUser(
    email: String, phone: Option[String]
  ): Future[Option[UserId]] = dao.addUser(User(0, email.toLowerCase(), phone))

  override def findUser(userId: UserId): Future[Option[User]] = {
    dao.findUser(userId)
  }
}
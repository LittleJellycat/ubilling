package ru.fintech.school.ubilling.handler

import ru.fintech.school.ubilling.dao.UserDao
import ru.fintech.school.ubilling.schema.TableDefinitions.{User, UserId}

import scala.concurrent.Future

trait UserService {
  def addUser(email: String, phone: Option[String]): Future[Either[Throwable, UserId]]

  def findUser(userId: UserId): Future[Option[User]]
}

class UserServiceImpl(dao: UserDao) extends UserService {
  override def addUser(
    email: String, phone: Option[String]
  ): Future[Either[Throwable, UserId]] = dao.addUser(User(0, email.toLowerCase(), phone))

  override def findUser(userId: UserId): Future[Option[User]] = {
    dao.findUser(userId)
  }

  def findUserByEmail(mail: String): Future[Option[User]] = {
    dao.findUserByEmail(mail)
  }
}
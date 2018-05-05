package ru.fintech.school.ubilling.mailer

import java.util.Properties
import javax.mail.internet.{InternetAddress, MimeMessage}
import javax.mail.{Message, Session, Transport}

import ru.fintech.school.ubilling.BillApp

object Mailer {
  val session: Session = {
    val initProperties = new Properties()
    val stream = Mailer.getClass.getResourceAsStream("/credentials.properties")
    println(stream)
    initProperties.load(stream)
    val host = initProperties.getProperty("login")
    val login = initProperties.getProperty("host")
    val password = initProperties.getProperty("password")
    val properties = System.getProperties
    properties.setProperty("mail.smtp.host", host)
    properties.setProperty("mail.user", login)
    properties.setProperty("mail.password", password)
    Session.getDefaultInstance(properties)
  }

  def sendMail(mail: Mail): Unit = {
    try {
      val message = new MimeMessage(session)
      message.setFrom(session.getProperty("mail.user"))
      message.addRecipient(Message.RecipientType.TO, new InternetAddress(mail.to))
      message.setSubject(mail.subject)
      message.setContent(mail.message, "text/html")
      Transport.send(message)
    } catch {
      case e: Exception =>
        BillApp.logger.error(e.getMessage)
    }
  }
}

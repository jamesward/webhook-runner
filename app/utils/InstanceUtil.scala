package utils

import play.api.libs.json.Json

import sys.process._
import scala.util.{Failure, Random, Success, Try}

object InstanceUtil {

  case class Info(project: String, zone: String, machineType: String, containerImage: String, name: Option[String] =  None) {
    lazy val validName = {
      val initialName = name.getOrElse(containerImage.replaceAll("[^0-9a-zA-Z]", "-").replace("--", "-")).toLowerCase

      initialName.headOption.fold {
        LazyList.continually(Random.shuffle(('a' to 'z').toList).head).take(8).mkString
      } { first =>
        if (Character.isAlphabetic(first.toInt))
          initialName
        else
          "x-" + initialName
      }
    }
  }

  implicit val infoReads = Json.reads[Info]

  def create(info: Info, maybeServiceAccount: Option[String]): Try[String] = {
    val cmd = s"""gcloud beta compute instances create-with-container
                 |${info.validName}
                 |--container-restart-policy=never
                 |--no-restart-on-failure
                 |--scopes=cloud-platform
                 |--container-image=${info.containerImage}
                 |--container-stdin
                 |--container-tty
                 |--no-address
                 |--zone=${info.zone}
                 |--project=${info.project}
                 |""".stripMargin.replace("\n", " ")

    run(cmd, maybeServiceAccount)
  }

  def delete(info: Info, maybeServiceAccount: Option[String]): Try[String] = {
    val cmd = s"""gcloud compute instances delete
                 |${info.validName}
                 |--quiet
                 |--zone=${info.zone}
                 |--project=${info.project}
                 |""".stripMargin.replace("\n", " ")

    run(cmd, maybeServiceAccount)
  }

  def describe(info: Info, maybeServiceAccount: Option[String]): Try[String] = {
    val cmd = s"""gcloud compute instances describe
                 |${info.validName}
                 |--zone=${info.zone}
                 |--project=${info.project}
                 |""".stripMargin.replace("\n", " ")

    run(cmd, maybeServiceAccount)
  }

  def update(info: Info, maybeServiceAccount: Option[String]): Try[String] = {
    val cmd = s"""gcloud compute instances update-container
                 |${info.validName}
                 |--container-image=${info.containerImage}
                 |--zone=${info.zone}
                 |--project=${info.project}
                 |""".stripMargin.replace("\n", " ")

    run(cmd, maybeServiceAccount)
  }

  def start(info: Info, maybeServiceAccount: Option[String]): Try[String] = {
    val cmd = s"""gcloud compute instances start
                 |${info.validName}
                 |--zone=${info.zone}
                 |--project=${info.project}
                 |""".stripMargin

    run(cmd, maybeServiceAccount)
  }

  def run(cmd: String, maybeServiceAccount: Option[String]): Try[String] = {

    val cmdWithMaybeServiceAccount = maybeServiceAccount.fold(cmd)(cmd + " --impersonate-service-account=" + _)

    sealed class Target
    case object Out extends Target
    case object Err extends Target

    trait AllLines {
      def allLines: String
    }

    val processLogger = new ProcessLogger with AllLines {
      private val lines = collection.mutable.Buffer[(String, Target)]()
      def allLines: String = lines.map(_._1).mkString("\n")

      override def out(s: => String): Unit = lines.append(s -> Out)
      override def err(s: => String): Unit = lines.append(s -> Err)
      override def buffer[T](f: => T): T = f
    }

    val result = cmdWithMaybeServiceAccount.run(processLogger)

    // todo: for now, do nothing with the output target
    if (result.exitValue() == 0)
      Success(processLogger.allLines)
    else
      Failure(ProcessFailed(cmdWithMaybeServiceAccount, processLogger.allLines))
  }

  case class ProcessFailed(cmd: String, out: String) extends Exception {
    override def getMessage: String = {
      s"""
         |Tried to run:
         |$cmd
         |
         |Resulted in:
         |$out
         |
         |""".stripMargin
    }
  }

}

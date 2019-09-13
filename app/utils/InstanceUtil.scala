package utils

import play.api.libs.json.Json

import sys.process._
import scala.util.{Failure, Random, Success, Try}

object InstanceUtil {

  case class Info(project: String, zone: String, machineType: String, containerImage: String, name: Option[String] =  None) {
    lazy val validName = {
      val initialName = name.getOrElse(containerImage.replaceAll("[^0-9a-zA-Z]", "-").replaceAllLiterally("--", "-")).toLowerCase

      initialName.headOption.fold {
        LazyList.continually(Random.shuffle(('a' to 'z').toList).head).take(16).mkString
      } { first =>
        if (Character.isAlphabetic(first))
          initialName
        else
          "x-" + initialName
      }
    }
  }

  implicit val infoReads = Json.reads[Info]

  def create(info: Info): Try[String] = {
    val cmd = s"""gcloud beta compute instances create-with-container
                 |${info.validName}
                 |--container-restart-policy=never
                 |--no-restart-on-failure
                 |--scopes=cloud-platform
                 |--container-image=${info.containerImage}
                 |--zone=${info.zone}
                 |--project=${info.project}
                 |""".stripMargin.replaceAllLiterally("\n", " ")

    run(cmd)
  }

  def delete(info: Info): Try[String] = {
    val cmd = s"""gcloud compute instances delete
                 |${info.validName}
                 |--quiet
                 |--zone=${info.zone}
                 |--project=${info.project}
                 |""".stripMargin.replaceAllLiterally("\n", " ")

    run(cmd)
  }

  def describe(info: Info): Try[String] = {
    val cmd = s"""gcloud compute instances describe
                 |${info.validName}
                 |--zone=${info.zone}
                 |--project=${info.project}
                 |""".stripMargin.replaceAllLiterally("\n", " ")

    run(cmd)
  }

  def update(info: Info): Try[String] = {
    val cmd = s"""gcloud compute instances update-container
                 |${info.validName}
                 |--container-image=${info.containerImage}
                 |--zone=${info.zone}
                 |--project=${info.project}
                 |""".stripMargin.replaceAllLiterally("\n", " ")

    run(cmd)
  }

  def start(info: Info): Try[String] = {
    val cmd = s"""gcloud compute instances start
                 |${info.validName}
                 |--zone=${info.zone}
                 |--project=${info.project}
                 |""".stripMargin

    run(cmd)
  }

  def run(cmd: String): Try[String] = {
    sealed class Target
    case object Out extends Target
    case object Err extends Target

    val processLogger = new ProcessLogger {
      val lines = collection.mutable.Buffer[(String, Target)]()

      override def out(s: => String): Unit = lines.append(s -> Out)
      override def err(s: => String): Unit = lines.append(s -> Err)
      override def buffer[T](f: => T): T = f
    }

    val result = cmd.run(processLogger)

    // todo: for now, do nothing with the output target
    if (result.exitValue() == 0)
      Success(processLogger.lines.map(_._1).mkString("\n"))
    else
      Failure(ProcessFailed(cmd, processLogger.lines.map(_._1).mkString("\n")))
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
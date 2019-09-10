package utils

import org.scalatestplus.play.PlaySpec
import utils.InstanceUtil.InstanceInfo
import org.scalatest.TryValues._

import scala.util.Random

class InstanceUtilSpec extends PlaySpec {

  val project = sys.env.getOrElse("PROJECT", throw new Exception("Must set PROJECT env var"))

  val name = Random.alphanumeric.take(16).mkString

  "an instance" must {
    "be creatable" in {
      val instanceInfo = InstanceInfo(project, "us-central1-a", "n1-standard-1", "docker.io/hello-world", Some(name))

      val operation = InstanceUtil.createInstance(instanceInfo)

      //operation.toEither.left.get.printStackTrace()
      println(operation.get)

      operation.get.getStatus must equal ("RUNNING")
    }
  }

}

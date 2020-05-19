package utils

import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpec}
import utils.InstanceUtil.Info

import scala.util.Random

class InstanceUtilSpec extends WordSpec with MustMatchers with BeforeAndAfterAll {

  val project = sys.env.getOrElse("PROJECT", throw new Exception("Must set PROJECT env var"))

  val maybeServiceAccount = sys.env.get("SERVICE_ACCOUNT")

  val name = Random.alphanumeric.take(8).mkString

  val instanceInfo = Info(project, "us-central1-a", "n1-standard-1", "docker.io/hello-world", Some(name))

  "instance name" must {
    "not start with a number" in {
      instanceInfo.copy(name = Some("0")).validName must equal ("x-0")
    }
    "not be empty" in {
      instanceInfo.copy(name = Some("")).validName.length must be > 0
    }
  }

  "an instance" must {
    "be creatable" in {
      val operation = InstanceUtil.create(instanceInfo, maybeServiceAccount)
      operation.get must endWith ("RUNNING")
    }
    "be creatable with an invalid name" in {
      val invalidInstanceInfo = instanceInfo.copy(name = Some(""))
      val operation = InstanceUtil.create(invalidInstanceInfo, maybeServiceAccount)
      InstanceUtil.delete(invalidInstanceInfo, maybeServiceAccount)
      operation.get must endWith ("RUNNING")
    }
    "be describable" in {
      val operation = InstanceUtil.describe(instanceInfo, maybeServiceAccount)
      operation.get must include ("status: RUNNING")
    }
    "fail when trying to describe an non-existent instance" in {
      val operation = InstanceUtil.describe(instanceInfo.copy(name = Some(Random.alphanumeric.take(8).mkString)), maybeServiceAccount)
      operation.isFailure must be (true)
    }
    "be updatable" in {
      val operation = InstanceUtil.update(instanceInfo, maybeServiceAccount)
      operation.get must endWith ("done.")
    }
    "be startable" in {
      val operation = InstanceUtil.start(instanceInfo, maybeServiceAccount)
      operation.get must include ("Updated")
    }
  }

  override protected def afterAll(): Unit = {
    if (InstanceUtil.describe(instanceInfo, maybeServiceAccount).isSuccess) {
      InstanceUtil.delete(instanceInfo, maybeServiceAccount)
    }
  }

}

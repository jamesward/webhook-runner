package utils

import java.io.File

import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpec}
import utils.InstanceUtil.Info

import scala.util.Random

class InstanceUtilSpec extends WordSpec with MustMatchers with BeforeAndAfterAll {

  val projectId = sys.env.getOrElse("PROJECT_ID", throw new Exception("Must set PROJECT_ID env var"))

  val maybeServiceAccount = sys.env.get("SERVICE_ACCOUNT")

  def randomName() = Random.alphanumeric.take(8).mkString
  val name = randomName()

  val instanceInfo = Info(projectId, "us-central1-a", "n1-standard-1", "docker.io/hello-world", Some(name))

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
      val operation = InstanceUtil.create(instanceInfo, None, maybeServiceAccount)
      operation.get must endWith ("RUNNING")
    }
    "be creatable with an invalid name" in {
      val invalidInstanceInfo = instanceInfo.copy(name = Some(""))
      val operation = InstanceUtil.create(invalidInstanceInfo, None, maybeServiceAccount)
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

  "the shutdown script" must {
    "shutdown an instance after the docker process stops" in {
      val tmpName = randomName()
      val instanceInfo = Info(projectId, "us-central1-a", "n1-standard-1", "docker.io/hello-world", Some(tmpName))

      val file = new File(getClass.getClassLoader.getResource("scripts/shutdown-on-docker-exit.sh").toURI)

      val createOperation = InstanceUtil.create(instanceInfo, Some(file), maybeServiceAccount)
      createOperation.get must endWith ("RUNNING")

      // wait for the shutdown script to run
      // todo: there's probably a better way to poll with a timeout
      Thread.sleep(180 * 1000)

      val operation = InstanceUtil.describe(instanceInfo, maybeServiceAccount)
      operation.get must (include ("status: TERMINATED") or include ("status: STOPPING"))

      // todo: delete even if the test failed
      InstanceUtil.delete(instanceInfo, maybeServiceAccount)
    }
  }

  override protected def afterAll(): Unit = {
    if (InstanceUtil.describe(instanceInfo, maybeServiceAccount).isSuccess) {
      InstanceUtil.delete(instanceInfo, maybeServiceAccount)
    }
  }

}

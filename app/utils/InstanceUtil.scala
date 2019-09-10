package utils

import com.google.cloud.compute.v1.{AttachedDisk, AttachedDiskInitializeParams, Instance, InstanceClient, Items, Metadata, NetworkInterface, Operation, ProjectZoneName, Scheduling, ServiceAccount}
import play.api.libs.json.Json

import scala.util.Try

object InstanceUtil {

  case class InstanceInfo(project: String, zone: String, machineType: String, containerImage: String, maybeName: Option[String] =  None) {
    // todo: must start with a letter
    lazy val name = maybeName.getOrElse(containerImage.replaceAll("[^0-9a-zA-Z]", "-").replaceAllLiterally("--", "-")).toLowerCase
  }

  implicit val instanceInfoReads = Json.reads[InstanceInfo]

  def createInstance(instanceInfo: InstanceInfo): Try[Operation] = {
    Try {
      val attachedDiskInitializeParams = AttachedDiskInitializeParams.newBuilder()
        .setSourceImage("projects/debian-cloud/global/images/family/debian-9")
        .setDiskSizeGb("10")
        .setDiskType(s"zones/${instanceInfo.zone}/diskTypes/pd-standard")
        .build()

      val disk = AttachedDisk.newBuilder()
        .setType("PERSISTENT")
        .setBoot(true)
        .setMode("READ_WRITE")
        .setAutoDelete(true)
        .setInitializeParams(attachedDiskInitializeParams)
        .build()

      val instance = Instance
        .newBuilder()
        .setName(instanceInfo.name)
        .setZone(instanceInfo.zone)
        .setMachineType(s"zones/${instanceInfo.zone}/machineTypes/${instanceInfo.machineType}")
        .setScheduling(Scheduling.newBuilder().setAutomaticRestart(false).build())
        .setMetadata(Metadata.newBuilder().addItems(Items.newBuilder().setKey("google-logging-enabled").setValue("true").build()).build())
        .addServiceAccounts(ServiceAccount.newBuilder().addScopes("https://www.googleapis.com/auth/cloud-platform").build())
        .addNetworkInterfaces(NetworkInterface.newBuilder().build())
        .addDisks(disk)
        .build()
      // todo: container image

      val instanceClient = InstanceClient.create()

      val projectZoneName = ProjectZoneName.of(instanceInfo.project, instanceInfo.zone)

      instanceClient.insertInstance(projectZoneName, instance)

    }
  }

}

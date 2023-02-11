// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.utility;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;

import org.photonvision.EstimatedRobotPose;
import org.photonvision.PhotonCamera;
import org.photonvision.PhotonPoseEstimator;
import org.photonvision.PhotonPoseEstimator.PoseStrategy;
import org.photonvision.targeting.PhotonPipelineResult;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.Pair;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.wpilibj.Filesystem;
public class PhotonVisionUtil{

  private final PhotonCamera[] cameras;
  private final Transform3d[] cameraPoses;

  private final AprilTagFieldLayout layout;
  private final Path fieldJsonPath = Paths.get(Filesystem.getDeployDirectory().toString(), "MS-Atrium-Temp.json");
  private final ArrayList<Pair<PhotonCamera, Transform3d>> cameraList = new ArrayList<Pair<PhotonCamera, Transform3d>>();

  private final ArrayList<PhotonPoseEstimator> poseEstimators = new ArrayList<PhotonPoseEstimator>();

  private Optional<EstimatedRobotPose> estPose = Optional.empty();

  /** Creates a new VisionSubsystem. */
  public PhotonVisionUtil(PhotonCamera[] cameras, Transform3d[] cameraPoses) {
    this.cameras = new PhotonCamera[cameras.length];
    this.cameraPoses = new  Transform3d[cameraPoses.length];
    for (int i = 0; i < cameras.length; i++) {
      this.cameras[i] = cameras[i];
      this.cameraPoses[i] = cameraPoses[i];
      cameraList.add(new Pair<PhotonCamera, Transform3d>(cameras[i], cameraPoses[i]));
    }

    try {
      layout = new AprilTagFieldLayout(fieldJsonPath);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    for (int i = 0; i < cameras.length; i++) {
      PhotonPoseEstimator pe = new PhotonPoseEstimator(layout, PoseStrategy.CLOSEST_TO_REFERENCE_POSE, cameras[i], cameraPoses[i]);
      poseEstimators.add(pe);
      pe.setReferencePose(new Pose2d());
    }
  }

  public void update() {
    ArrayList<Optional<EstimatedRobotPose>> poseGuesses = new ArrayList<Optional<EstimatedRobotPose>>();
    ArrayList<PhotonPipelineResult> pipelineResults = new ArrayList<PhotonPipelineResult>();

    for (PhotonCamera c : cameras) {
      PhotonPipelineResult pr = c.getLatestResult();
      pipelineResults.add(pr);
    }

    for (int i = 0; i < pipelineResults.size(); i++) {
      if (pipelineResults.get(i).hasTargets()) {
        poseGuesses.add(poseEstimators.get(i).update());
      }
    }

    if (!poseGuesses.isEmpty()) {
      if (poseGuesses.get(0).isPresent()) {
        estPose = poseGuesses.get(0);
      }
    }
  }

  public void setReferencePose(Pose2d pose) {
    for (PhotonPoseEstimator pe : poseEstimators) {
      pe.setReferencePose(pose);
    }
  }

  public Optional<EstimatedRobotPose> getRobotPose3d () {
    if (estPose != null) {
      return estPose;
    } else {
      EstimatedRobotPose e  = new EstimatedRobotPose(new Pose3d(), 0);
      return Optional.of(e);
    }
  }
}
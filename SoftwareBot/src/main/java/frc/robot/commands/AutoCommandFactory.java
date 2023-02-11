
package frc.robot.commands;

import java.util.HashMap;

import com.pathplanner.lib.PathConstraints;
import com.pathplanner.lib.PathPlanner;
import com.pathplanner.lib.PathPlannerTrajectory;
import com.pathplanner.lib.auto.PIDConstants;
import com.pathplanner.lib.auto.SwerveAutoBuilder;

import edu.wpi.first.wpilibj2.command.Command;

import frc.robot.subsystems.drivetrain.DrivetrainSubsystem;

public final class AutoCommandFactory {

    private static PathConstraints pathConstraints = new PathConstraints(4.0, 3.0);
    

    public static Command createNullAuto() {
        return null;
    }
    
    public static Command createTestAuto(DrivetrainSubsystem drivetrain) {

        PathPlannerTrajectory path = PathPlanner.loadPath("TestPath", pathConstraints);
        HashMap<String, Command> eventMap = new HashMap<>();

        SwerveAutoBuilder builder = new SwerveAutoBuilder(
                () -> drivetrain.getPose(),
                (pose) -> drivetrain.resetPose(pose),
                new PIDConstants(5.0, 0, 0),
                new PIDConstants(0.5, 0, 0),
                (chassisSpeeds) -> drivetrain.setTargetChassisVelocity(chassisSpeeds),
                eventMap,
                false,
                drivetrain);

        return builder.fullAuto(path);
    }

    // Default constructor that just throws an exception if you attempt to create an
    // instace of this class.
    private AutoCommandFactory() {
        throw new UnsupportedOperationException("This is a static class, you cannont instantiate it.");
    }
}
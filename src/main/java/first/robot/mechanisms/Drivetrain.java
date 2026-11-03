package first.robot.mechanisms;

import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.MotorAlignmentValue;
import first.robot.simulation.DrivetrainSim;
import java.util.function.DoubleSupplier;
import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.drive.DifferentialDrive;
import org.wpilib.framework.RobotBase;
import org.wpilib.hardware.bus.CANPort;
import org.wpilib.hardware.imu.OnboardIMU;
import org.wpilib.hardware.imu.OnboardIMU.MountOrientation;

public class Drivetrain implements Mechanism {
  private final TalonFX leftLeader = new TalonFX(0, new CANBus(CANPort.CAN_S0));
  private final TalonFX leftFollower = new TalonFX(1, new CANBus(CANPort.CAN_S0));
  private final TalonFX rightLeader = new TalonFX(2, new CANBus(CANPort.CAN_S0));
  private final TalonFX rightFollower = new TalonFX(3, new CANBus(CANPort.CAN_S0));

  public final DifferentialDrive drivetrain =
      new DifferentialDrive(leftLeader::setThrottle, rightLeader::setThrottle);

  private final OnboardIMU imu = new OnboardIMU(MountOrientation.FLAT);

  private final DrivetrainSim drivetrainSim = new DrivetrainSim(leftLeader, rightLeader);

  public Drivetrain() {
    TalonFXConfiguration leftConfig = new TalonFXConfiguration();
    leftConfig.MotorOutput.withInverted(InvertedValue.Clockwise_Positive);
    leftLeader.getConfigurator().apply(leftConfig);

    TalonFXConfiguration rightConfig = new TalonFXConfiguration();
    rightConfig.MotorOutput.withInverted(InvertedValue.CounterClockwise_Positive);
    rightLeader.getConfigurator().apply(rightConfig);

    leftFollower.setControl(new Follower(leftLeader.getDeviceID(), MotorAlignmentValue.Aligned));
    rightFollower.setControl(new Follower(rightLeader.getDeviceID(), MotorAlignmentValue.Aligned));

    setDefaultCommand(idle());
  }

  public Command idle() {
    return run(coroutine -> {
          while (true) {
            drivetrain.arcadeDrive(0, 0);
            coroutine.yield();
          }
        })
        .named("Idle");
  }

  public Command arcadeDrive(DoubleSupplier throttle, DoubleSupplier rotation) {
    return run(coroutine -> {
          while (true) {
            drivetrain.arcadeDrive(throttle.getAsDouble(), rotation.getAsDouble());
            coroutine.yield();
          }
        })
        .named("Drive");
  }

  public void periodic() {
    if (RobotBase.isSimulation()) {
      drivetrainSim.periodic();
    }
  }
}

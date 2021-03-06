package org.firstinspires.ftc.teamcode.CircuitRunners.Auton;

import com.arcrobotics.ftclib.geometry.Rotation2d;
import com.arcrobotics.ftclib.geometry.Translation2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.CircuitRunners.Auton.subsystems.*;
import org.firstinspires.ftc.teamcode.CircuitRunners.Auton.commands.*;
import org.firstinspires.ftc.teamcode.CircuitRunners.RobotUtil;

public class AutoRed extends LinearOpMode {


    private IntakeSubsystem intake;// = new IntakeSubsystem(this);
    private LiftSubsystem lift;// = new LiftSubsystem(this);
    private VisionSubsystem vision;// = new VisionSubsystem(this);
    private DriveSubsystem drive; // = new DriveSubsystem(this);


    private static int skystonePos = -1;

    @Override
    public void runOpMode() throws InterruptedException {


        intake = new IntakeSubsystem(this);
        lift = new LiftSubsystem(this);
        vision = new VisionSubsystem(this);
        drive = new DriveSubsystem(this);

        addLog("Subsystems Initializing...");
        telemetry.update();

        intake.initialize();
        lift.initialize();
        vision.initialize();
        drive.initialize();

        //For intake
        IntakeCommand intakeCommand = new IntakeCommand(intake, IntakeSubsystem.Direction.IN);

        //For grabber
        GrabberCommand grabberCommand = new GrabberCommand(lift, GrabberCommand.GrabberPos.OPEN);

        //For 4bar
        Move4BCommand move4BCommand = new Move4BCommand(lift, Move4BCommand.v4BPos.IN);

        //For drive base
        DriveTranslationCommand translationCommand = new DriveTranslationCommand(
                drive,
                DriveSubsystem.Direction.FORWARD,
                new Translation2d(0, RobotUtil.toCm(12)),
                0.9
        );

        DriveTurnCommand turnCommand = new DriveTurnCommand(
                drive,
                DriveSubsystem.Direction.COUNTERCLOCK,
                new Rotation2d(Math.PI / 2),
                0.9
        );

        DriveTranslationCommand driveToFirstStone = new DriveTranslationCommand(
                drive,
                DriveSubsystem.Direction.RIGHT,
                new Translation2d(0, RobotUtil.toCm(24 * 2 - 27)),
                0.8
        );

        DriveTurnCommand turnToFirstStone = new DriveTurnCommand(
                drive,
                DriveSubsystem.Direction.CLOCK,
                new Rotation2d(Math.toRadians(60)),
                0.3
        );

        DriveTranslationCommand driveIntoFirstStone = new DriveTranslationCommand(
                drive,
                DriveSubsystem.Direction.FORWARD,
                new Translation2d(-3 * Math.cos(Math.toRadians(60)),
                        RobotUtil.toCm(24 * 2 - 27) * Math.toRadians(60)),
                0.2
        );

        DriveTurnCommand turnFromFirstStone = new DriveTurnCommand(
                drive,
                DriveSubsystem.Direction.COUNTERCLOCK,
                new Rotation2d(Math.PI / 2),
                0.3
        );

        DriveTranslationCommand backAwayFromFirstStone = new DriveTranslationCommand(
                drive,
                DriveSubsystem.Direction.BACKWARDS,
                new Translation2d(0, RobotUtil.toCm(24 * 2 - 27)),
                0.2
        );

        //Things to be moved on init
        addLog("Moving grabber to init position...");
        telemetry.update();
        grabberCommand.initialize(); //Grabber init pos
        doAndEnd(grabberCommand);

        addLog("Initialized...");
        addLog("Starting Vision...");
        telemetry.update();

        vision.startStreaming();
        addLog("Started...");
        while(!isStarted() && !isStopRequested()){
            vision.addTelemetry();
            telemetry.update();
            //The use of the vision subsystem is special in that it's not used through a command
            //(If you make a command for vision, there is something wrong with you)
        }
        vision.stopStreaming();
        skystonePos = vision.getSkystonePos();

        //Move intake out after sizing

        intakeCommand.initialize();
        intakeCommand.execute();
        sleep(500);
        intakeCommand.end();

        //Move forward a little
        translationCommand.initialize();
        waitForDrive(translationCommand);

        //Turn to face left parallel to stones
        turnCommand.initialize();
        waitForDrive(turnCommand);

        //No matter where the stone is, the grabber is going to close. Might as well set it up
        grabberCommand = new GrabberCommand(lift, GrabberCommand.GrabberPos.CLOSE);

        switch(skystonePos){
            case 0: //Left
                //Drive to get there
                driveToFirstStone.initialize();
                waitForDrive(driveToFirstStone);

                turnToFirstStone.initialize();
                waitForDrive(turnToFirstStone);

                //Start up intake
                intakeCommand.initialize();
                intakeCommand.execute();

                //Drive forward to intake stone
                driveIntoFirstStone.initialize();
                waitForDrive(driveIntoFirstStone);

                sleep(500); //Wait a second to ensure stone is in
                intakeCommand.end();

                //Grab stone in preparation
                grabberCommand.initialize();
                doAndEnd(grabberCommand);

                turnFromFirstStone.initialize();
                waitForDrive(turnFromFirstStone);

                backAwayFromFirstStone.initialize();
                waitForDrive(backAwayFromFirstStone);

                //Drive under bridge (maybe to foundation)

                break;
            case 1: //Center
                //Drive to get there

                //Start up intake
                intakeCommand.initialize();
                intakeCommand.execute();

                //Drive forward to intake stone


                sleep(500); //Wait a second to ensure stone is in
                intakeCommand.end();

                //Grab stone in preparation
                grabberCommand.initialize();
                doAndEnd(grabberCommand);

                //Drive under bridge (maybe to foundation)

                break;
            case 2: //Right
                //Drive to get there

                //Start up intake
                intakeCommand.initialize();
                intakeCommand.execute();

                //Drive forward to intake stone


                sleep(500); //Wait a second to ensure stone is in
                intakeCommand.end();

                //Grab stone in preparation
                grabberCommand.initialize();
                doAndEnd(grabberCommand);

                //Drive under bridge (maybe to foundation)

                break;
        }

        //Swing out stone
        move4BCommand.initialize();
        doAndEnd(move4BCommand);
        //Wait a little to ensure that it has finished
        sleep(500);

        //Drop stone
        grabberCommand = new GrabberCommand(lift, GrabberCommand.GrabberPos.OPEN);
        grabberCommand.initialize();
        doAndEnd(grabberCommand);
        sleep(500);

        //Move 4bar back inside
        move4BCommand = new Move4BCommand(lift, Move4BCommand.v4BPos.IN);
        move4BCommand.initialize();
        doAndEnd(move4BCommand);
        sleep(500);

        //Drive back to bridge


        //DONE

        //AT THE END
        intake.stop();
        lift.stop();
        drive.stop();
        vision.stop(); //<- Just for continuity
    }
    private void waitForDrive(Command command){
        while (opModeIsActive()){
            if(command.isFinished()){
                command.end();
                break;
            }
        }
    }

    private void doAndEnd(Command command){
        command.execute();
        command.end();
    }

    private void addLog(String data){
        telemetry.log().add(data);
    }
}

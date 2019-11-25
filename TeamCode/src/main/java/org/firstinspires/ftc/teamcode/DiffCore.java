package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;


import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Math.Vector;

import static org.firstinspires.ftc.teamcode.DiffConstants.INTAKE_POWER;

@TeleOp(name="core1.4.9",group="Diff")

public class DiffCore extends OpMode {
    private ElapsedTime runtime = new ElapsedTime();
    //top left and bottom right motors bad.
    private DcMotor motor1, motor2, motor3, motor4, leftIntake, rightIntake;
    private Cassette leftDrive;
    private Cassette rightDrive;
    private String scaleString;

    static double masterScale=.2;
    private FtcDashboard dashboard;
    private Telemetry dashTelemetry;

    private HardwareMap hardwareMap;
    private Telemetry telemetry;
    private Gamepad gamepad1,gamepad2;

    public DiffCore(OpMode opmode){

        hardwareMap=opmode.hardwareMap;
        telemetry=opmode.telemetry;
        gamepad1=opmode.gamepad1;
        gamepad2=opmode.gamepad2;
    }
    public void init(){
        motor1=hardwareMap.dcMotor.get("topL");
        motor2=hardwareMap.dcMotor.get("bottomL");
        motor3=hardwareMap.dcMotor.get("topR");
        motor4=hardwareMap.dcMotor.get("bottomR");
        leftIntake=hardwareMap.dcMotor.get("intakeL");
        rightIntake=hardwareMap.dcMotor.get("intakeR");

        telemetry=new MultipleTelemetry(telemetry,FtcDashboard.getInstance().getTelemetry());

        gamepad1.setJoystickDeadzone(.1f);
        fastMode();
        leftDrive=new Cassette(motor1,motor2,Math.toRadians(180),"LEFTDRIVE",false);
        rightDrive=new Cassette(motor3,motor4,Math.toRadians(0),"RIGHTDRIVE",true);

        resetEncoders();

    }
    public void loop(){
        //main loop
        diffDrive(gamepad1.left_stick_x,gamepad1.left_stick_y);
        logMotorStats();
        logCasseteStats();
//        update();
    }

    public void update() {}


    public void start(){
        runtime.reset();
        leftDrive.resetRuntime();
        rightDrive.resetRuntime();
    }


    @Deprecated public void DiffAutoDrive(double angle, double power){

    }


//WIP
    private void logCasseteStats(){
        telemetry.addData("Cassete1: ",leftDrive.basicTelemetry());
        telemetry.addData("Cassete2: ",rightDrive.basicTelemetry());
    }
    private void diffDrive(double stickLX,double stickLY) {
        Vector direction = new Vector(stickLX, -stickLY);
        double wheelMagnitude = direction.getMagnitude();
        double wheelAngle = direction.getAngle(true);
        leftDrive.update(wheelAngle,wheelMagnitude);
        rightDrive.update(wheelAngle,-wheelMagnitude);
        leftIntake.setPower(gamepad2.right_trigger - gamepad2.left_trigger);
        rightIntake.setPower(-1 * (gamepad2.right_trigger - gamepad2.left_trigger));
    }
    // will use for zeroing purposes.
     private void resetEncoders(){
        leftDrive.resetEncoders();
        rightDrive.resetEncoders();
    }

    private void logMotorStats(){
        telemetry.addData("Left Drive: ",leftDrive.getLogString());
        telemetry.addData("Right Drive: ",rightDrive.getLogString());
        telemetry.addData("Intake Power: ", new String(leftIntake.getPower());
        telemetry.addData("Mode: ",scaleString);

    }



    private void fastMode() {
        masterScale = 0.7;
        scaleString="i am speed";
    }
    private void slowMode(){
        masterScale = 0.2;
        scaleString="i am not speed";
    }
    //return true if input is detected on either stick
    //false if otherwise
}



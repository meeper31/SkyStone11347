package org.firstinspires.ftc.teamcode.CircuitRunners.Auton.subsystems;

import com.arcrobotics.ftclib.controller.PIDFController;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.openftc.revextensions2.ExpansionHubMotor;

public class LiftSubsystem implements Subsystem {


    private PIDFController liftController = new PIDFController(new double[] {1, .2, 0, 0.7});

    //Lift motors
    private ExpansionHubMotor lift_left, lift_right;

    //v4b servos
    private Servo leftFB, rightFB;

    //Grabber servo
    private Servo grab;

    private static final double TOLERANCE  = 50;

    private LinearOpMode opMode;

    /*
    So the mentality behind this system is the following. The measured value (pv) is always updated,
    and when there is manual control active the setpoint (sv) is also updated. When manual control stops,
    the pv is still updated but the sv stops being updated. The output of the controller is only applied
    when there is no manual controller
     */
    public LiftSubsystem(LinearOpMode opMode){
        this.opMode = opMode;
        lift_left = opMode.hardwareMap.get(ExpansionHubMotor.class, "lift_left");
        lift_right = opMode.hardwareMap.get(ExpansionHubMotor.class, "lift_right");

        lift_right.setDirection(ExpansionHubMotor.Direction.REVERSE);
        lift_left.setZeroPowerBehavior(ExpansionHubMotor.ZeroPowerBehavior.BRAKE);
        lift_right.setZeroPowerBehavior(ExpansionHubMotor.ZeroPowerBehavior.BRAKE);
        resetEncoders();
        setMode(ExpansionHubMotor.RunMode.RUN_USING_ENCODER);
        stoplift();

        leftFB = opMode.hardwareMap.servo.get("leftFB");
        rightFB = opMode.hardwareMap.servo.get("rightFB");
        leftFB.setDirection(Servo.Direction.REVERSE);

        grab = opMode.hardwareMap.servo.get("grabber");


        liftController.setTolerance(TOLERANCE);
    }

    @Override
    public void initialize(){
        stoplift();
    }

    /**
     * This is a blocking method!
     * @param pos the encoder pos
     */
    public void goTo(double pos){
        liftController.setSetPoint(pos);
        while(!liftController.atSetPoint() && opMode.opModeIsActive()){
            setPower(liftController.calculate(currentPos()));
        }
        stoplift();
    }

    public void setGrabPos(double pos){
        grab.setPosition(pos);
    }

    public double getGrabPos() { return grab.getPosition(); }

    public void set4BPos(double pos){
        leftFB.setPosition(pos);
        rightFB.setPosition(pos);
    }

    public double get4BPos() { return (leftFB.getPosition() + rightFB.getPosition()) / 2; }

    public double getTolerance() {
        return TOLERANCE;
    }

    @Override
    public void stop(){
        stoplift();
    }

    //Returns the current position of the lift
    public double currentPos(){
        return (lift_left.getCurrentPosition() + lift_right.getCurrentPosition()) / 2;
    }

    public void setPower(double power){
        lift_left.setPower(power);
        lift_right.setPower(power);
    }

    private void stoplift(){
        setPower(0);
        liftController.reset();
    }

    private void setMode(ExpansionHubMotor.RunMode mode){
        lift_left.setMode(mode);
        lift_right.setMode(mode);
    }

    private void resetEncoders(){
        setMode(ExpansionHubMotor.RunMode.STOP_AND_RESET_ENCODER);
    }
}

package org.firstinspires.ftc.teamcode;
import android.os.SystemClock;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.Math.ModuleFunctions;
public class Cassete {
    private double wheelAngle;
    private double topEncoder;
    private double bottomEncoder;
    private double currentTargetAngle = 0;
    private double angleToTurnAt = 0;
    private double currentForwardsPower = 0;

    private DcMotor topmotor;
    private DcMotor bottommotor;

    private double currentAngle_rad = 0; //real angle
    private double previousAngle_rad = 0; // angle from previous update, used for velocity

    private double angleError = 0;
    private double turnPower = 0; //not user input, calculated based on error
    private double currentTurnVelocity = 0; //current rate at which the module is turning

    private long currentTimeNanos = 0; //current time on the clock
    private long lastTimeNanos = 0; //previous update's clock time
    private double elapsedTimeThisUpdate = 0; //time of the update

    private double motor1Power = 0;
    private double motor2Power = 0;

    private double turnErrorSum = 0;


    public Cassete(DcMotor motor1, DcMotor motor2) {
        this.topmotor = motor1;
        this.bottommotor = motor2;
        motor1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        motor2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
    }

    public double getWheelAngle() {
        return wheelAngle;
    }

    public double getDeltaEncoder(DcMotor top, DcMotor bottom) {
        return ((top.getCurrentPosition() + bottom.getCurrentPosition()) / 2.0);
    }

    public void setDriveTrainDirection(double amountForwards, double amountSideWays,
                                       double amountTurn) {

        double xComponent = amountForwards * 1 + amountSideWays * 0 +
                Math.cos(angleToTurnAt) * amountTurn;
        double yComponent = amountForwards * 0 + amountSideWays * 1 +
                Math.sin(angleToTurnAt) * amountTurn;


        currentForwardsPower = Math.hypot(xComponent, yComponent);
        if (Math.abs(currentForwardsPower) > 0.03) {
            currentTargetAngle = Math.atan2(yComponent, xComponent);
        }
    }

        public void resetEncoders(){
            topmotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            bottommotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        }
    public void calculatePowersFixed(double rawTargetAngle, double wheelPower) {
//        if(reversed){wheelPower *= -1;}//reversed this if we are reversed
        currentTimeNanos = SystemClock.elapsedRealtimeNanos();
        elapsedTimeThisUpdate = (currentTimeNanos - lastTimeNanos)/1e9;

        if(elapsedTimeThisUpdate < 0.003){
            return;//don't do anything if it is too fast
        }
        //remember the time to calculate delta the next update
        lastTimeNanos = currentTimeNanos;
        //if there has been an outrageously long amount of time, don't bother
        if(elapsedTimeThisUpdate > 1){
            return;
        }


        //calculate our current angle
        currentAngle_rad = ModuleFunctions.calculateAngle(topmotor.getCurrentPosition(),
                bottommotor.getCurrentPosition());


        angleError = ModuleFunctions.subtractAngles(rawTargetAngle,currentAngle_rad);
        //we should never turn more than 180 degrees, just reverse the direction
        while (Math.abs(angleError) > Math.toRadians(90)) {
            if(rawTargetAngle > currentAngle_rad){
                rawTargetAngle -= Math.toRadians(180);
            }else{
                rawTargetAngle += Math.toRadians(180);
            }
            wheelPower *= -1;
            angleError = ModuleFunctions.subtractAngles(rawTargetAngle,currentAngle_rad);
        }

        double angleErrorVelocity = angleError -
                ((getCurrentTurnVelocity() / Math.toRadians(300)) * Math.toRadians(30)
                        * 0.22);//myRobot.getDouble("d"));




        turnErrorSum += angleError * elapsedTimeThisUpdate;

        if(Math.abs(Math.toDegrees(getCurrentTurnVelocity())) > 1100){
            //reset the error sum if going too fast
            turnErrorSum = 0;
        }






        //calculate the turn power
        turnPower = Range.clip((angleErrorVelocity / Math.toRadians(15)),-1,1)
                * 0.05;//myRobot.getDouble("p");
        turnPower += turnErrorSum * 0.075;//myRobot.getDouble("i");


        turnPower *= Range.clip(Math.abs(angleError)/Math.toRadians(2),0,1);

        //remember the angle
        previousAngle_rad = currentAngle_rad;

        //don't go until we get to the target position
        if(Math.abs(angleError) > Math.toRadians(20)){
            wheelPower = 0;
        }

        motor1Power = wheelPower * DiffCore.masterScale + turnPower * 1.0;
        motor2Power = -wheelPower * DiffCore.masterScale + turnPower * 1.0;

        maximumPowerScale();
    }
    public double getCurrentTurnVelocity() {
        return currentTurnVelocity;
    }

}


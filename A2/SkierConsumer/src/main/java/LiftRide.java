/*
 * Ski Data API for NEU Seattle distributed systems course
 * An API for an emulation of skier managment system for RFID tagged lift tickets. Basis for CS6650 Assignments for 2019
 *
 * OpenAPI spec version: 2.0
 *
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

//package io.swagger.client.model;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

/**
 * LiftRide
 */

//@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2024-10-05T21:22:04.664984076Z[GMT]")

public class LiftRide {
    @SerializedName("time")
    private Integer time = null;

    @SerializedName("liftID")
    private Integer liftID = null;

    public LiftRide time(Integer time) {
        this.time = time;
        return this;
    }

    /**
     * Get time
     * @return time
     **/
//    @Schema(example = "217", description = "")
    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public LiftRide liftID(Integer liftID) {
        this.liftID = liftID;
        return this;
    }

    /**
     * Get liftID
     * @return liftID
     **/
//    @Schema(example = "21", description = "")
    public Integer getLiftID() {
        return liftID;
    }

    public void setLiftID(Integer liftID) {
        this.liftID = liftID;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LiftRide liftRide = (LiftRide) o;
        return Objects.equals(this.time, liftRide.time) &&
                Objects.equals(this.liftID, liftRide.liftID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, liftID);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LiftRide {\n");

        sb.append("    time: ").append(toIndentedString(time)).append("\n");
        sb.append("    liftID: ").append(toIndentedString(liftID)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}
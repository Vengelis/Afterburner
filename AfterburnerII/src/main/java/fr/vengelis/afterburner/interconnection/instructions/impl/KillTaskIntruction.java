package fr.vengelis.afterburner.interconnection.instructions.impl;

import com.google.gson.Gson;
import fr.vengelis.afterburner.AfterburnerApp;
import fr.vengelis.afterburner.interconnection.instructions.BaseCommunicationInstruction;

public class KillTaskIntruction extends BaseCommunicationInstruction<Boolean> {

    public enum InputType {
        BASIC_MACHINE_NAME_STRING,
        JSON_MACHINE_NAME_STRING,
        NO_MACHINE_NAME,
        ;
    }

    private final String machineName;
    private final InputType inputType;
    private final String orderedBy;

    public KillTaskIntruction(String machineName, InputType inputType) {
        this.machineName = machineName;
        this.inputType = inputType;
        this.orderedBy = "lambda";
    }

    public KillTaskIntruction(String machineName, InputType inputType, String orderedBy) {
        this.machineName = machineName;
        this.inputType = inputType;
        this.orderedBy = orderedBy;
    }

    @Override
    public Boolean execute() {
        if(inputType.equals(InputType.JSON_MACHINE_NAME_STRING)) {
            if(machineName != null && !machineName.isEmpty()) {
                String worker = new Gson().fromJson(machineName, String.class);
                if(worker.equalsIgnoreCase(AfterburnerApp.get().getMachineName()) ||
                        worker.equalsIgnoreCase(AfterburnerApp.get().getUniqueId().toString()))
                    AfterburnerApp.get().killTask("Ordered by " + this.orderedBy);
                return true;
            }
        } else if(inputType.equals(InputType.BASIC_MACHINE_NAME_STRING)) {
            if(machineName != null && !machineName.isEmpty()) {
                if(machineName.equalsIgnoreCase(AfterburnerApp.get().getMachineName()) ||
                        machineName.equalsIgnoreCase(AfterburnerApp.get().getUniqueId().toString()))
                    AfterburnerApp.get().killTask("Ordered by " + this.orderedBy);
                return true;
            }
        } else if(inputType.equals(InputType.NO_MACHINE_NAME)) {
            AfterburnerApp.get().killTask("Ordered by " + this.orderedBy);
        }
        return false;
    }
}
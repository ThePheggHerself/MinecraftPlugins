package phewitch.modbox.Commands.CommandBase;

import phewitch.modbox.ModBox;

public class BypassCommand {
    String bypassPermission = "";

    public String getBypassPermission(){
        return ModBox.Instance.getName() + "."+bypassPermission;
    }

    public void setBypassPermission(String permission){
        bypassPermission = permission;
    }
}

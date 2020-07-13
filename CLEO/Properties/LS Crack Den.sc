// For GTA3script https://www.mixmods.com.br/2017/01/tutorial-como-criar-mods-cleos.html
SCRIPT_START
{
    LVAR_INT iState // External script will start this and set the first var to state.
    LVAR_INT scplayer hGenerator hBlip

    CONST_INT STATE_NOT_STARTED_BY_EXTERNAL 0 // remembering, the default value for vars is always 0
    CONST_INT STATE_JUST_BOUGHT 1
    CONST_INT STATE_ALREADY_BOUGHT 2

    // This will terminate this script if was not started from external.
    // This occurs if you try to install it on modloader or CLEO root folder.
    IF iState = STATE_NOT_STARTED_BY_EXTERNAL
        TERMINATE_THIS_CUSTOM_SCRIPT
    ENDIF
    // Note: you can't use SAVE_THIS_CUSTOM_SCRIPT in this case, but if you really need it, make this script do something for other scripts that uses.

    // Do stuff if just bought (create car generators, radar icons, enable interiors etc... Maybe some additional message)
    // Called just 1 time.
    IF iState = STATE_JUST_BOUGHT
        // Caution, GTA SA can't handle too much car generators, will need some limit adjuster.
        // You may want to use STATE_ALREADY_BOUGHT, check distance (LOCATE) and create the car with CREATE_CAR when the player is near.
        CREATE_CAR_GENERATOR 2162.1714 -1677.8451 15.0859 130.0 521 3 3 TRUE 0 0 0 2000 (hGenerator)
        SWITCH_CAR_GENERATOR hGenerator 101 // make it spawns forever
        //SET_CAR_GENERATOR_NO_SAVE hGenerator // If you don't want to be saved on your saved game (from CLEO+)
    ENDIF
     
    // Do stuff for already bought properties.
    // This is called every time the game starts with the property purchased. ALSO just after you bought it.
    IF iState = STATE_ALREADY_BOUGHT
        // Maybe you will want to process something for that property here, maybe some garage door, play some ambient music... Maybe some mission?
        // You can even "sell" the property if you delete the pickup and terminate this script. The pickup will be recreated in the next time you start the game.
    ENDIF

}
SCRIPT_END

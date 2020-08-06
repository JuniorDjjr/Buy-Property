// You need: https://forum.mixmods.com.br/f16-utilidades/t179-gta3script-while-true-return_true-e-return_false#p1159
// And: https://www.mixmods.com.br/2020/03/CLEOPlus.html
SCRIPT_START
{
    LVAR_FLOAT x y z // In (external script will call this sending the pickup position here, don't change the vars order)
    LVAR_INT scplayer hPickup i bPickupFromSavedGame

    WAIT 1000
 
    // If not called by external script, just terminate it (remember, all vars are 0 by default)
    IF x = 0.0
    AND y = 0.0
    AND z = 0.0
        CLEO_CALL WriteGlobalVar 0 (409 0)() // reset ONMISSION during game start
        TERMINATE_THIS_CUSTOM_SCRIPT
    ENDIF
 
    // A simple save pickup...
    // SAVE_THIS_CUSTOM_SCRIPT is NOT required.
    // The pickup will be stored in the saved game

    bPickupFromSavedGame = FALSE
    IF GET_PICKUP_THIS_COORD x y z TRUE hPickup
        GET_PICKUP_MODEL hPickup i
        IF i = 1277 //pickupsave
            REMOVE_PICKUP hPickup
        ENDIF
    ENDIF

    GET_PLAYER_CHAR 0 scplayer
    
    WHILE TRUE
        GOSUB CreatePickupWait
        WHILE TRUE
            WAIT 0
            IF HAS_PICKUP_BEEN_COLLECTED hPickup
                REMOVE_PICKUP hPickup
                SET_CHAR_COORDINATES_NO_OFFSET scplayer x y z
                SET_CHAR_HEADING scplayer 0.0
                RESTORE_CAMERA_JUMPCUT
                SET_CAMERA_BEHIND_PLAYER
                CLEO_CALL WriteGlobalVar 0 (409 1)()
                SET_PLAYER_CONTROL 0 FALSE
                DO_FADE 1 1000
                WAIT 0
                ACTIVATE_SAVE_MENU
                WAIT 500
                SET_PLAYER_CONTROL 0 TRUE
                WAIT 500
                CLEO_CALL WriteGlobalVar 0 (409 0)()
                WAIT 2000
                GOSUB CreatePickupWait
            ELSE
                IF IS_ON_MISSION
                    REMOVE_PICKUP hPickup
                    BREAK // break to first WHILE
                ENDIF
            ENDIF
        ENDWHILE
    ENDWHILE

    CreatePickupWait:
    // Wait until isn't on mission
    WHILE IS_ON_MISSION
        WAIT 0
    ENDWHILE
    
    // Way to get out from pickup point
    // Why 2D? Maybe the player is falling in the same xy eternality,
    // this way he would be going up and getting the pickup again and again (wtf)
    WHILE LOCATE_CHAR_ANY_MEANS_2D scplayer x y 3.0 3.0 FALSE
        WAIT 0
    ENDWHILE
    IF bPickupFromSavedGame = FALSE
        CREATE_PICKUP 1277 3 x y z hPickup
    ELSE
        bPickupFromSavedGame = FALSE
    ENDIF
    RETURN
}
SCRIPT_END

{
    LVAR_INT var value //In
    LVAR_INT scriptSpace finalOffset

    WriteGlobalVar:
    READ_MEMORY 0x00468D5E 4 1 (scriptSpace)
    finalOffset = var * 4
    finalOffset += scriptSpace
    WRITE_MEMORY finalOffset 4 (value) FALSE
    CLEO_RETURN 0 ()
}

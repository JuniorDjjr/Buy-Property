// You need: https://forum.mixmods.com.br/f16-utilidades/t179-gta3script-while-true-return_true-e-return_false#p1159
SCRIPT_START
{
    LVAR_FLOAT x y z // In (external script will call this sending the pickup position here, don't change the vars order)
    LVAR_INT scplayer hPickup

    // If not called by external script, just terminate it (remember, all vars are 0 by default)
    IF x = 0.0
    AND y = 0.0
    AND z = 0.0
        TERMINATE_THIS_CUSTOM_SCRIPT
    ENDIF

    // A simple save pickup...
    // SAVE_THIS_CUSTOM_SCRIPT is NOT required.

    GET_PLAYER_CHAR 0 scplayer
    
    WHILE TRUE
        GOSUB CreatePickupWaitPlayer
        WHILE TRUE
            WAIT 0
            IF HAS_PICKUP_BEEN_COLLECTED hPickup
                REMOVE_PICKUP hPickup
                SET_CHAR_COORDINATES_NO_OFFSET scplayer x y z
                SET_CHAR_HEADING scplayer 0.0
                ACTIVATE_SAVE_MENU 
                WAIT 2000
                GOSUB CreatePickupWaitPlayer
            ELSE
                IF NOT LOCATE_CHAR_ANY_MEANS_3D scplayer x y z 100.0 100.0 50.0 FALSE
                    REMOVE_PICKUP hPickup
                    BREAK // break to first WHILE
                ENDIF
            ENDIF
        ENDWHILE
    ENDWHILE

    CreatePickupWaitPlayer:
    // Wait until the player is near.
    WHILE NOT LOCATE_CHAR_ANY_MEANS_3D scplayer x y z 100.0 100.0 50.0 FALSE
        WAIT 0
    ENDWHILE
    // Way to get out from pickup point
    // Why 2D? Maybe the player is falling in the same xy eternality,
    // this way he would be going up and getting the pickup again and again (wtf)
    WHILE LOCATE_CHAR_ANY_MEANS_2D scplayer x y 3.0 3.0 FALSE
        WAIT 0
    ENDWHILE
    CREATE_PICKUP 1277 5 x y z hPickup
    RETURN
}
SCRIPT_END

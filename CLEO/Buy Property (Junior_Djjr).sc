// You need: https://forum.mixmods.com.br/f16-utilidades/t179-gta3script-while-true-return_true-e-return_false
// And: https://www.mixmods.com.br/2020/03/CLEOPlus.html
SCRIPT_START
{
    LVAR_INT pInString // In
    LVAR_INT pString iPrice iProfit scplayer hPickup pPickup pStartPickups pEndPickups bAlreadyCreated iSizeOfPickup i j k pStartScriptBuffer bOnlySave hBlip iUnlockedAfter
    LVAR_FLOAT x y z x2 y2 z2 camX camY camZ saveX saveY saveZ 
    LVAR_TEXT_LABEL tEnableInterior

    GET_LABEL_POINTER Buffer pString

    IF pInString = 0 // First thread (root)
        IF FIND_FIRST_FILE "CLEO\Properties\*.ini" (i pString)
            WHILE TRUE
                STREAM_CUSTOM_SCRIPT "Buy Property (Junior_Djjr).cs" pString
                WAIT 0 // to make .cs use the allocate memory var, so we can change it again
                IF NOT FIND_NEXT_FILE i (pString)
                    BREAK
                ENDIF 
            ENDWHILE
        ENDIF
        TERMINATE_THIS_CUSTOM_SCRIPT
    ENDIF
   
    // Each thread starts with pInString as .ini file name
    STRING_FORMAT pString "CLEO\Properties\%s" $pInString

    GET_LABEL_POINTER StartScriptBuffer pStartScriptBuffer
    STRING_FORMAT pStartScriptBuffer "Properties\%s" $pInString
    // Change file name from .ini to .cs
    j = pStartScriptBuffer
    WHILE TRUE
        READ_MEMORY j 1 FALSE (k)
        IF NOT k = 0
            IF k = 46
                j++
                WRITE_MEMORY j 1 99 FALSE //'c'
                j++
                WRITE_MEMORY j 1 115 FALSE //'s'
                j++
                WRITE_MEMORY j 1 0 FALSE
                BREAK
            ENDIF
        ELSE
            BREAK
        ENDIF
        j++
    ENDWHILE

    GET_LABEL_POINTER Buffer2 pInString

    IF READ_STRING_FROM_INI_FILE $pString "Settings" "BuyCoord" (pInString)
        IF NOT SCAN_STRING $pInString "%f %f %f" i x y z
            PRINT_FORMATTED_NOW "~r~Error: Unable to read 'BuyCoord' from %s" 10000 $pString
            TERMINATE_THIS_CUSTOM_SCRIPT
        ENDIF
    ELSE
        PRINT_FORMATTED_NOW "~r~Error: Unable to read 'BuyCoord' from %s" 10000 $pString
        TERMINATE_THIS_CUSTOM_SCRIPT
    ENDIF

    IF NOT READ_INT_FROM_INI_FILE $pString "Settings" "Price" (iPrice)
        PRINT_FORMATTED_NOW "~r~Error: Unable to read 'Price' from %s" 10000 $pString
        TERMINATE_THIS_CUSTOM_SCRIPT
    ENDIF

    IF NOT READ_INT_FROM_INI_FILE $pString "Settings" "OnlySavePickup" (bOnlySave)
        PRINT_FORMATTED_NOW "~r~Error: Unable to read 'OnlySavePickup' from %s" 10000 $pString
        TERMINATE_THIS_CUSTOM_SCRIPT
    ENDIF

    IF NOT READ_INT_FROM_INI_FILE $pString "Settings" "Profit" (iProfit)
        IF bOnlySave = FALSE
            PRINT_FORMATTED_NOW "~r~Error: Unable to read 'Profit' from %s." 10000 $pString
            TERMINATE_THIS_CUSTOM_SCRIPT
        ENDIF
    ENDIF

    IF NOT READ_INT_FROM_INI_FILE $pString "Settings" "UnlockedAfter" (iUnlockedAfter)
        iUnlockedAfter = 0
    ENDIF

    IF READ_STRING_FROM_INI_FILE $pString "Settings" "SavePickupCoord" (pInString)
        IF NOT SCAN_STRING $pInString "%f %f %f" i saveX saveY saveZ
            saveX = 0.0
        ENDIF
    ENDIF
    
    // We can't use SAVE_THIS_CUSTOM_SCRIPT in this case because we are reusing the same script
    // Just checking if pickup is created there is a good way too, I don't like CLEO save system
    bAlreadyCreated = FALSE
 
    IF GET_PICKUP_THIS_COORD x y z TRUE hPickup
        GET_PICKUP_MODEL hPickup i
        IF i = 1274 // bigdollar 
            GOSUB CallAlreadyBought
            TERMINATE_THIS_CUSTOM_SCRIPT
        ELSE
            bAlreadyCreated = TRUE
        ENDIF
    ELSE
        IF GET_PICKUP_THIS_COORD saveX saveY saveZ TRUE hPickup
            GET_PICKUP_MODEL hPickup i
            IF i = 1277 //pickupsave
                GOSUB CallAlreadyBought
                TERMINATE_THIS_CUSTOM_SCRIPT
            ENDIF
        ENDIF
    ENDIF

    GET_INT_STAT 181 i
    IF i >= iUnlockedAfter
        k = TRUE
    ELSE
        k = FALSE
    ENDIF

    IF k = TRUE
        IF bAlreadyCreated = FALSE
            CREATE_FORSALE_PROPERTY_PICKUP x y z iPrice PROP_3 hPickup
        ENDIF
        ADD_CLEO_BLIP 31 x y TRUE 255 255 255 255 (hBlip)
    ELSE
        IF bAlreadyCreated = FALSE
            CREATE_LOCKED_PROPERTY_PICKUP x y z PROP_4 hPickup
        ENDIF
        ADD_CLEO_BLIP 32 x y TRUE 255 255 255 255 (hBlip)
    ENDIF

    WHILE TRUE
        WAIT 0
        IF k = FALSE // locked
            GET_INT_STAT 181 i
            IF i >= iUnlockedAfter
                k = TRUE
                // recreate if unlocked now
                REMOVE_PICKUP hPickup
                REMOVE_CLEO_BLIP hBlip
                CREATE_FORSALE_PROPERTY_PICKUP x y z iPrice PROP_3 hPickup
                ADD_CLEO_BLIP 31 x y TRUE 255 255 255 255 (hBlip)
            ENDIF
        ELSE
            IF HAS_PICKUP_BEEN_COLLECTED hPickup
                REMOVE_PICKUP hPickup
                BREAK
            ENDIF
        ENDIF
    ENDWHILE

    REMOVE_CLEO_BLIP hBlip

    IF bOnlySave = FALSE
        CREATE_PROTECTION_PICKUP x y z iProfit iProfit (hPickup)
    ENDIF

    SWITCH_WIDESCREEN ON
    SET_PLAYER_CONTROL 0 FALSE
    PLAY_MISSION_PASSED_TUNE 2
    INCREMENT_INT_STAT 15 iPrice
    PRINT_BIG BUYPRO 5000 2
    
    STREAM_CUSTOM_SCRIPT $pStartScriptBuffer 1
    WAIT 0
    GOSUB CallAlreadyBought
    
    IF READ_STRING_FROM_INI_FILE $pString "Settings" "EnableInterior" (tEnableInterior)
        SWITCH_ENTRY_EXIT $tEnableInterior ON
    ENDIF

    IF READ_STRING_FROM_INI_FILE $pString "Settings" "CamCoord" (pInString)
        IF SCAN_STRING $pInString "%f %f %f" i camX camY camZ
        AND NOT camX = 0.0
        
            SET_FIXED_CAMERA_POSITION camX camY camZ 0.0 0.0 0.0

            camX = 0.0
            IF READ_STRING_FROM_INI_FILE $pString "Settings" "CamPoint" (pInString)
                SCAN_STRING $pInString "%f %f %f" i camX camY camZ
            ENDIF

            IF NOT camX = 0.0
                POINT_CAMERA_AT_POINT camX camY camZ 2
            ELSE
                POINT_CAMERA_AT_POINT x y z 2
            ENDIF
            
            WAIT 5000
            RESTORE_CAMERA_JUMPCUT
        ENDIF
    ENDIF

    SWITCH_WIDESCREEN OFF
    SET_PLAYER_CONTROL 0 TRUE

    TERMINATE_THIS_CUSTOM_SCRIPT


    CallAlreadyBought:
    IF READ_INT_FROM_INI_FILE $pString "Settings" "RadarIcon" (i)
    AND i >= 0
        ADD_CLEO_BLIP i x y TRUE 255 255 255 255 hBlip
    ENDIF
    STREAM_CUSTOM_SCRIPT $pStartScriptBuffer 2
    IF NOT saveX = 0.0
        STREAM_CUSTOM_SCRIPT "Buy Property - save pickup system.cs" saveX saveY saveZ
    ENDIF
    RETURN
}
SCRIPT_END

Buffer:
DUMP
00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 
ENDDUMP

Buffer2:
DUMP
00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 
ENDDUMP

StartScriptBuffer:
DUMP
00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 
ENDDUMP

// You need: https://forum.mixmods.com.br/f16-utilidades/t179-gta3script-while-true-return_true-e-return_false
// And: https://www.mixmods.com.br/2020/03/CLEOPlus.html
SCRIPT_START
{
    LVAR_INT pInString // In
    LVAR_INT pString iPrice iProfit hPickup pStartPickups bAlreadyCreated iSizeOfPickup i j k l pStartScriptBuffer bOnlySave hBlip iUnlockedAfter iJustSavedSot
    LVAR_FLOAT x y z x2 y2 z2 camX camY camZ saveX saveY saveZ saveAngle
    LVAR_TEXT_LABEL tEnableInterior

    GET_LABEL_POINTER Buffer pString

    IF pInString = 0 // First script (root)

        GET_CURRENT_SAVE_SLOT j
        IF j >= 0

            GET_LABEL_POINTER Buffer3 (pInString)
            STRING_FORMAT pInString "CLEO\Properties\Save%d\*.ini" j

            CREATE_LIST DATATYPE_INT l

            IF FIND_FIRST_FILE $pInString (i pString)
                WHILE TRUE
                    GET_STRING_LENGTH $pString (k)
                    IF k > 0
                        IF CLEO_CALL UninstallFileIsValid 0 (pString j)
                            ALLOCATE_MEMORY k (k)
                            COPY_STRING $pString k
                            LIST_ADD l k
                        ENDIF
                    ENDIF
                    IF NOT FIND_NEXT_FILE i (pString)
                        BREAK
                    ENDIF 
                ENDWHILE
            ENDIF
        ELSE
            j = -1
        ENDIF

        IF FIND_FIRST_FILE "CLEO\Properties\*.ini" (i pString)
            IF NOT j = -1
                GET_LIST_SIZE l (k)
            ENDIF
            WHILE TRUE

                IF NOT j = -1
                    // find ini in save list, if exists, delete it
                    timera = 0
                    WHILE timera < k
                        GET_LIST_VALUE_BY_INDEX l timera (pInString)
                        IF IS_STRING_EQUAL $pInString $pString 128 FALSE ""
                            FREE_MEMORY pInString
                            LIST_REMOVE_INDEX l timera
                            BREAK
                        ENDIF
                        ++timera
                    ENDWHILE
                ENDIF

                STREAM_CUSTOM_SCRIPT "Buy Property (Junior_Djjr).cs" pString
                WAIT 0 // to make .cs use the allocate memory var, so we can change it again
                IF NOT FIND_NEXT_FILE i (pString)
                    BREAK
                ENDIF 
            ENDWHILE
        ENDIF

        //PRINT_FORMATTED_NOW "%d" 10000 j

        IF NOT j = -1
            GET_LIST_SIZE l (k)
            i = 0
            WHILE i < k
                WAIT 2000
                GET_LIST_VALUE_BY_INDEX l i (pInString)
                //PRINT_FORMATTED_NOW "%s" 10000 $pInString
                CLEO_CALL Uninstall 0 (pInString)
                ++i
            ENDWHILE
        ENDIF

        // Keep monitoring save to delete uinstall files
        iJustSavedSot = -1
        SET_SCRIPT_EVENT_SAVE_CONFIRMATION ON OnSaveConfirmation iJustSavedSot
        
        WHILE TRUE
            WAIT 0
            IF iJustSavedSot >= 0
                // IMPORTANT: only delete if saved on same loaded game
                IF iJustSavedSot = j
                    CLEO_CALL DeleteUninstallFiles 0 (l j)
                ENDIF
                BREAK
            ENDIF
        ENDWHILE
        TERMINATE_THIS_CUSTOM_SCRIPT
    ENDIF
   
    // Each script starts with pInString as .ini file name
    STRING_FORMAT pString "CLEO\Properties\%s" $pInString

    GET_LABEL_POINTER IniFileBuffer pStartScriptBuffer
    STRING_FORMAT pStartScriptBuffer "%s" $pInString

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
        ELSE
            READ_FLOAT_FROM_INI_FILE $pString "Settings" "Angle" (saveAngle)
        ENDIF
    ENDIF
    
    // We can't use SAVE_THIS_CUSTOM_SCRIPT in this case because we are reusing the same script
    // Just checking if pickup is created there is a good way too, I don't like CLEO save system
    bAlreadyCreated = FALSE

    iJustSavedSot = -1
    SET_SCRIPT_EVENT_SAVE_CONFIRMATION ON OnSaveConfirmation iJustSavedSot
 
    IF GET_PICKUP_THIS_COORD x y z TRUE hPickup
        GET_PICKUP_MODEL hPickup i
        IF i = 1274 // bigdollar 
            GOSUB CallAlreadyBought
            GOSUB InfiniteLoop
        ELSE
            bAlreadyCreated = TRUE
        ENDIF
    ELSE
        IF GET_PICKUP_THIS_COORD saveX saveY saveZ TRUE hPickup
            GET_PICKUP_MODEL hPickup i
            IF i = 1277 //pickupsave
                GOSUB CallAlreadyBought
                GOSUB InfiniteLoop
            ENDIF
        ENDIF
    ENDIF

    
    IF READ_INT_FROM_INI_FILE $pString "Settings" "DisableInteriorByDefault" (i)
    AND i = TRUE
        IF READ_STRING_FROM_INI_FILE $pString "Settings" "EnableInterior" (tEnableInterior)
            SWITCH_ENTRY_EXIT $tEnableInterior OFF
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
        GOSUB Process
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

    GOSUB InfiniteLoop
    TERMINATE_THIS_CUSTOM_SCRIPT


    InfiniteLoop:
    WHILE TRUE
        GOSUB Process
    ENDWHILE
    RETURN

    Process:
    WAIT 0
    IF iJustSavedSot >= 0
        GET_LABEL_POINTER Buffer3 k
        GET_LABEL_POINTER IniFileBuffer j
        STRING_FORMAT k "CLEO\Properties\Save%d" iJustSavedSot
        CREATE_DIRECTORY $k
        STRING_FORMAT k "CLEO\Properties\Save%d\%s" iJustSavedSot $j
        GET_LABEL_POINTER Buffer2 pInString

        WRITE_INT_TO_INI_FILE 0 $k "Uninstall" "CanBeRemoved"

        STRING_FORMAT pInString "%.3f %.3f %.3f" saveX saveY saveZ
        WRITE_STRING_TO_INI_FILE $pInString $k "Uninstall" "SavePickup"

        STRING_FORMAT pInString "%.3f %.3f %.3f" x y z
        WRITE_STRING_TO_INI_FILE $pInString $k "Uninstall" "BuyPickup"
        
        IF READ_INT_FROM_INI_FILE $pString "Settings" "DisableInteriorByDefault" (i)
        AND i = TRUE
            STRING_FORMAT pInString "%s" $tEnableInterior
        ELSE
            STRING_FORMAT pInString ""
        ENDIF
        WRITE_STRING_TO_INI_FILE $pInString $k "Uninstall" "InteriorDisableByDefault"

        iJustSavedSot = -1
    ENDIF
    RETURN


    CallAlreadyBought:
    IF READ_INT_FROM_INI_FILE $pString "Settings" "RadarIcon" (i)
    AND i >= 0
        ADD_CLEO_BLIP i x y TRUE 255 255 255 255 hBlip
    ENDIF
    GET_PLAYER_CHAR 0 (k)
    IF LOCATE_CHAR_DISTANCE_TO_COORDINATES k saveX saveY saveZ 2.0
        SET_CHAR_HEADING k saveAngle
    ENDIF
    STREAM_CUSTOM_SCRIPT $pStartScriptBuffer 2
    IF NOT saveX = 0.0
        STREAM_CUSTOM_SCRIPT "Buy Property - save pickup system.cs" saveX saveY saveZ
    ENDIF
    RETURN

    OnSaveConfirmation:
    // var iJustSavedSot will be filled here
    // this can also be called by root script
    RETURN_SCRIPT_EVENT
}
SCRIPT_END

{
    LVAR_INT pIniName //In
    LVAR_INT pBuffer pBuffer2 i hPickup
    LVAR_FLOAT x y z
    LVAR_TEXT_LABEL tInteriorName

    Uninstall:
    GET_LABEL_POINTER Buffer (pBuffer)
    GET_LABEL_POINTER Buffer2 (pBuffer2)
    GET_CURRENT_SAVE_SLOT i
    STRING_FORMAT pBuffer "CLEO\Properties\Save%d\%s" i pIniName

    IF READ_STRING_FROM_INI_FILE $pBuffer "Uninstall" "SavePickup" (pBuffer2)
        IF SCAN_STRING $pBuffer2 "%f %f %f" i x y z

            IF GET_PICKUP_THIS_COORD x y z TRUE hPickup
                GET_PICKUP_MODEL hPickup i
                IF i = 1277 //pickupsave
                    REMOVE_PICKUP hPickup
                    //PRINT_STRING_NOW "delete save" 10000
                ENDIF
            ENDIF

        ENDIF
    ENDIF

    IF READ_STRING_FROM_INI_FILE $pBuffer "Uninstall" "BuyPickup" (pBuffer2)
        IF SCAN_STRING $pBuffer2 "%f %f %f" i x y z

            IF GET_PICKUP_THIS_COORD x y z TRUE hPickup
                GET_PICKUP_MODEL hPickup i
                IF i = 1274 // bigdollar 
                OR i = 1273 // property_fsale 
                OR i = 1272 // property_locked 
                    REMOVE_PICKUP hPickup
                    //PRINT_STRING_NOW "deleted buy" 10000
                ENDIF
            ENDIF

        ENDIF
    ENDIF

    IF READ_STRING_FROM_INI_FILE $pBuffer "Uninstall" "InteriorDisableByDefault" (tInteriorName)
        SWITCH_ENTRY_EXIT $tInteriorName OFF
    ENDIF

    CLEO_RETURN 0 ()
}

{
    LVAR_INT lList iLoadedSaveSlot //In
    LVAR_INT i iListSize pIniName pBuffer

    DeleteUninstallFiles:
    GET_LABEL_POINTER Buffer (pBuffer)
    GET_LIST_SIZE lList (iListSize)
    WHILE i < iListSize
        GET_LIST_VALUE_BY_INDEX lList i (pIniName)
        STRING_FORMAT pBuffer "CLEO\Properties\Save%d\%s" iLoadedSaveSlot pIniName
        //PRINT_FORMATTED_NOW "Deleting uninstall file '%s'" 10000 $pBuffer
        //WAIT 1000
        // TODO: there is a problem with CLEO library when deleting a file from var
        //DELETE_FILE $pBuffer
        // So, at least keep some flag inside the file
        WRITE_INT_TO_INI_FILE 1 $pBuffer "Uninstall" "CanBeRemoved"
        ++i
    ENDWHILE
    CLEO_RETURN 0 ()
}

{
    LVAR_INT pIniName iLoadedSaveSlot //In
    LVAR_INT i pBuffer

    UninstallFileIsValid:
    GET_LABEL_POINTER Buffer2 (pBuffer)
    STRING_FORMAT pBuffer "CLEO\Properties\Save%d\%s" iLoadedSaveSlot pIniName
    IF READ_INT_FROM_INI_FILE $pBuffer "Uninstall" "CanBeRemoved" (i)
    AND i = FALSE
        RETURN_TRUE
        CLEO_RETURN 0 ()
    ENDIF
    RETURN_FALSE
    CLEO_RETURN 0 ()
}


Buffer:
DUMP
00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 
00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 
ENDDUMP

Buffer2:
DUMP
00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 
00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 
ENDDUMP

Buffer3:
DUMP
00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 
00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 
ENDDUMP

StartScriptBuffer:
DUMP
00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 
ENDDUMP

IniFileBuffer:
DUMP
00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 
ENDDUMP

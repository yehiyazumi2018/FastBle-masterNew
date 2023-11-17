package com.clj.bleapp.operation;

import static android.os.Build.VERSION.SDK_INT;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.clj.bleapp.Data;
import com.clj.bleapp.R;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class CharacteristicOperationFragment extends Fragment {
    private static final int PERMISSION_REQUEST_CODE = 100;
    public static final int PROPERTY_READ = 1;
    public static final int PROPERTY_WRITE = 2;
    public static final int PROPERTY_WRITE_NO_RESPONSE = 3;
    public static final int PROPERTY_NOTIFY = 4;
    public static final int PROPERTY_INDICATE = 5;
    private Button Start, stop, Btn_clear, Btn_ciread, Btn_ciwrite, MA_Btnwrite, MA_Btnread;
    private Spinner Data_spinner, Buffer_spinner, Adress_spinner, Size_Spinner;
    private LinearLayout layout_container;
    private final List<String> childList = new ArrayList<>();

    private final byte bMode = (byte) 0x80;
    private final byte bDestAddress = 0x60;
    private final byte bToolAddress = (byte) 0xF1;
    private final byte bSize = 0x01;
    private final byte bSID = 0;
    private final byte bCheckSum = 0;
    private final byte bIDOption = (byte) 0x88;

    public boolean stopreceive = false, Read = false, Btnciread = false;
    private EditText Et_modules;

    EditText Et_CIDatalayerID, Et_CIBuffer, Et_MA_Address, Et_MA_Size, Et_MA_Buffer;

    ScrollView scrollView;

    byte[] value = new byte[7];

    int[] calchecksumvalues = new int[20];
    String strText = "";

    public CharacteristicOperationFragment() {
    }

    public enum DL_Data_T_All {
        WARNING_LIGHT_01_STATE,
        WARNING_LIGHT_01_PWM_DC,
        WARNING_LIGHT_02_STATE,
        WARNING_LIGHT_02_PWM_DC
    }

    BleDevice bleDevice;
    BluetoothGattCharacteristic characteristic;
    String Final_packecttosend = "";
    String hex = "";
    TextView txt;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_characteric_operation, null);
        initView(v);
        return v;
    }

    private void initView(View v) {
        layout_container = v.findViewById(R.id.layout_container);


    }


    public void showData() {
        bleDevice = ((OperationActivity) getActivity()).getBleDevice();
        characteristic = ((OperationActivity) getActivity()).getCharacteristic();
        final int charaProp = ((OperationActivity) getActivity()).getCharaProp() - 1;
        String child = characteristic.getUuid().toString();

        for (int i = 0; i < layout_container.getChildCount(); i++) {
            layout_container.getChildAt(i).setVisibility(View.GONE);
        }

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteric_operation, null);
        //final Spinner SP_module = view.findViewById(R.id.SP_module);
        Spinner SP_DBVariable = view.findViewById(R.id.SP_DBVariable);

        BufferedReader reader;
        int intLoop = 0;
        List<Data> liData = new ArrayList<Data>();
        Data context = new Data(0, "START_U8BIT");

        String da1 = "";
        try {

            String filename = "";
            String dirPath = "";
            File dir;

            String strDestinationDirPath = "";

            if (Build.VERSION_CODES.R == SDK_INT) {
                dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Documents/VariablesFile";
            } else {
                dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath() + "/VariablesFile";
            }

            dir = new File(dirPath);

            if (!dir.exists()) {
                AssetManager assetManager = getActivity().getAssets();
                InputStream in = null;
                OutputStream out = null;

                try {
                    dir.mkdirs();

                    String[] arrFileName = {"Data_Layer.h", "J1939Data_Layer.h", "EE_PH_DB.h"};
                    for (int i = 0; i < 3; i++) {
                        filename = arrFileName[i];
                        in = assetManager.open(filename);
                        File outFile = new File(dirPath, filename);
                        out = new FileOutputStream(outFile);
                        copyFile(in, out);
                    }
                } catch (Exception ex) {
                    //Toast.makeText(getActivity(), ex.toString(), Toast.LENGTH_LONG).show();
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Exception ex) {

                        }
                    }
                    if (out != null) {
                        try {
                            out.close();
                        } catch (Exception ex) {

                        }
                    }
                }
            }
            else
            {
                File[] files = dir.listFiles();
                if (files != null) {
                    int numberOfFiles = files.length;

                    if (numberOfFiles == 0) {
                        AssetManager assetManager = getActivity().getAssets();
                        InputStream in = null;
                        OutputStream out = null;

                        try {
                            String[] arrFileName = {"Data_Layer.h", "J1939Data_Layer.h", "EE_PH_DB.h"};
                            for (int i = 0; i < 3; i++) {
                                filename = arrFileName[i];
                                in = assetManager.open(filename);
                                File outFile = new File(dirPath, filename);
                                out = new FileOutputStream(outFile);
                                copyFile(in, out);
                            }
                        } catch (Exception ex) {
                            //Toast.makeText(getActivity(), ex.toString(), Toast.LENGTH_LONG).show();
                        } finally {
                            if (in != null) {
                                try {
                                    in.close();
                                } catch (Exception ex) {

                                }
                            }
                            if (out != null) {
                                try {
                                    out.close();
                                } catch (Exception ex) {

                                }
                            }
                        }
                    }
                }
            }

            try {
                File[] files = dir.listFiles();
                if (files != null) {
                    int numberOfFiles = files.length;

                    if (numberOfFiles == 0) {
                        Toast.makeText(getActivity(), "Files not available in 'VariablesFiles' Folder", Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                File file1 = new File(dir, "Data_Layer.h");
                BufferedReader br = new BufferedReader(new FileReader(file1));

                while ((strText = br.readLine()) != null) {
                    if (strText != null && !strText.isEmpty() && !strText.equals("null")) {
                        if (!strText.equals("") && strText.trim().contains(",") && (!strText.trim().toUpperCase().contains("DUMMY") || strText.trim().toUpperCase().contains("U8_DUMMY") || strText.trim().toUpperCase().contains("WRU8_DUMMY") || strText.trim().toUpperCase().contains("U16_DUMMY") || strText.trim().toUpperCase().contains("WRU16_DUMMY") || strText.trim().toUpperCase().contains("U32_DUMMY") || strText.trim().toUpperCase().contains("U64_DUMMY") || strText.trim().toUpperCase().contains("S8_DUMMY") || strText.trim().toUpperCase().contains("S16_DUMMY") || strText.trim().toUpperCase().contains("S32_DUMMY") || strText.trim().toUpperCase().contains("FLOAT_DUMMY") || strText.trim().toUpperCase().contains("BUF512BIT_DUMMY")) && !strText.trim().toUpperCase().startsWith("//") && !strText.trim().toUpperCase().contains("UNAUTHORIZED")) {
                            String[] arrLineText = strText.trim().split(",");
                            String strFinalText = arrLineText[0];

                            context = new Data(intLoop, strFinalText);
                            context.setDataId(intLoop);
                            context.setData(strFinalText);

                            liData.add(context);
                            intLoop += 1;
                        }
                    }

                    if (strText.trim().contains("START_EEPROM")) {
                        //third file

                        File file3 = new File(dir, "EE_PH_DB.h");
                        BufferedReader br2 = new BufferedReader(new FileReader(file3));

                        while ((strText = br2.readLine()) != null) {
                            if (strText != null && !strText.isEmpty() && !strText.equals("null")) {
                                if (!strText.equals("") && strText.trim().contains(",") && (!strText.trim().toUpperCase().contains("DUMMY") || strText.trim().toUpperCase().contains("U8_DUMMY") || strText.trim().toUpperCase().contains("WRU8_DUMMY") || strText.trim().toUpperCase().contains("U16_DUMMY") || strText.trim().toUpperCase().contains("WRU16_DUMMY") || strText.trim().toUpperCase().contains("U32_DUMMY") || strText.trim().toUpperCase().contains("U64_DUMMY") || strText.trim().toUpperCase().contains("S8_DUMMY") || strText.trim().toUpperCase().contains("S16_DUMMY") || strText.trim().toUpperCase().contains("S32_DUMMY") || strText.trim().toUpperCase().contains("FLOAT_DUMMY") || strText.trim().toUpperCase().contains("BUF512BIT_DUMMY")) && !strText.trim().toUpperCase().startsWith("//") && !strText.trim().toUpperCase().contains("UNAUTHORIZED")) {
                                    String[] arrLineText = strText.trim().split(",");
                                    String strFinalText = arrLineText[0];

                                    context = new Data(intLoop, strFinalText);
                                    context.setDataId(intLoop);
                                    context.setData(strFinalText);
                                    liData.add(context);
                                    intLoop += 1;
                                }
                            }
                        }
                        br2.close();
                        break;
                    }

                    if (strText.trim().contains("J1939Data_Layer.h")) {
                        break;
                    }
                }
                br.close();

                //second file

                File file2 = new File(dir, "J1939Data_Layer.h");
                BufferedReader br1 = new BufferedReader(new FileReader(file2));

                while ((strText = br1.readLine()) != null) {
                    if (strText != null && !strText.isEmpty() && !strText.equals("null")) {
                        if (!strText.equals("") && strText.trim().contains(",") && (!strText.trim().toUpperCase().contains("DUMMY") || strText.trim().toUpperCase().contains("U8_DUMMY") || strText.trim().toUpperCase().contains("WRU8_DUMMY") || strText.trim().toUpperCase().contains("U16_DUMMY") || strText.trim().toUpperCase().contains("WRU16_DUMMY") || strText.trim().toUpperCase().contains("U32_DUMMY") || strText.trim().toUpperCase().contains("U64_DUMMY") || strText.trim().toUpperCase().contains("S8_DUMMY") || strText.trim().toUpperCase().contains("S16_DUMMY") || strText.trim().toUpperCase().contains("S32_DUMMY") || strText.trim().toUpperCase().contains("FLOAT_DUMMY") || strText.trim().toUpperCase().contains("BUF512BIT_DUMMY")) && !strText.trim().toUpperCase().startsWith("//") && !strText.trim().toUpperCase().contains("UNAUTHORIZED")) {
                            String[] arrLineText = strText.trim().split(",");
                            String strFinalText = arrLineText[0];

                            context = new Data(intLoop, strFinalText);
                            context.setDataId(intLoop);
                            context.setData(strFinalText);
                            liData.add(context);
                            intLoop += 1;
                        }
                    }
                    if (strText.trim().contains("EE_PH_DB.h")) {
                        break;
                    }
                }
                br1.close();


            } catch (IOException e) {
                e.printStackTrace();
            }


        } catch (Exception ex) {
            Log.d("ioException", ex.toString());
        }


        ArrayAdapter adapter = new ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, liData);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        SP_DBVariable.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        SP_DBVariable.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String strHexValue = Integer.toString(position, 16);

                String strSelVal = "";

                if (strHexValue.length() == 1) {
                    strSelVal = "0" + strHexValue;
                } else {
                    strSelVal = strHexValue;
                }

                Et_CIDatalayerID.setText(strSelVal);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /*SP_DBVariable.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View arg1,
                                       int position, long id) {
                Object item = parent.getItemAtPosition(position);
                String ID = String.valueOf(SP_DBVariable.getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });*/


        //Edittext
        Et_CIDatalayerID = view.findViewById(R.id.Et_CIDatalayerID);
        Et_CIBuffer = view.findViewById(R.id.Et_CIBuffer);
        Et_MA_Address = view.findViewById(R.id.Et_MA_Address);
        Et_MA_Size = view.findViewById(R.id.Et_MA_Size);
        Et_MA_Buffer = view.findViewById(R.id.Et_MA_Buffer);

        scrollView = view.findViewById(R.id.scrollView);

        // view.setTag(bleDevice.getKey() + characteristic.getUuid().toString() + charaProp);
        LinearLayout layout_add = view.findViewById(R.id.layout_add);
        final TextView txt_title = view.findViewById(R.id.txt_title);
        //txt_title.setText(String.valueOf(characteristic.getUuid().toString() + getActivity().getString(R.string.data_changed)));
        txt = view.findViewById(R.id.txt);
        txt.setMovementMethod(ScrollingMovementMethod.getInstance());

        View add_view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteric_operation, null);
        //write
        final EditText Et_Send = view.findViewById(R.id.Et_Send);
        Button btn = view.findViewById(R.id.btn);

        txt.setMovementMethod(new ScrollingMovementMethod());


        //Read data for start and stop
        Thread t1 = new Thread(new Runnable() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void run() {

                //Read
                while (true) {
                    if (Read) {
                        try {
                            Thread.sleep(1000);
                            Log.d("Read", "Reading...");
                            if (stopreceive) {
                                stopreceive = false;
                                Read = false;
                            }

                            fnThread_BLERead();

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        });
        t1.start();

        //Clear Terminal
        Btn_clear = view.findViewById(R.id.Btn_clear);
        Btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txt.setText("");
            }
        });

        /*Start button*/
        Start = view.findViewById(R.id.Btn_start);
        Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Read = true;
                hex = "8060f1018153";
                if (TextUtils.isEmpty(hex)) {
                    return;
                }

                //start write
                fnBtn_Start_BLEWrite();
            }
        });

        /* stop button */
        stop = view.findViewById(R.id.Btn_stop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Read = true;

                stopreceive = true;

                hex = "8060f1018254";
                if (TextUtils.isEmpty(hex)) {
                    return;
                }
                //stop
                fnBtn_Stop_BLEWrite();
            }
        });


        Button Btn_writemodules = view.findViewById(R.id.Btn_writemodules);
        Btn_writemodules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Read = true;
                hex = Et_modules.getText().toString();
                if (TextUtils.isEmpty(hex)) {
                    return;
                }


                fnBtn_writemodules_BLEWrite();
            }
        });


        //write for Data by Common Identifier

        Btn_ciwrite = view.findViewById(R.id.Btn_ciwrite);
        Btn_ciwrite.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {
                try {
                    Read = true;
                    int packet_size = 0;
                    String Total_Size_of_Packet = "";
                    String packet_header = "8060f1";
                    String command = "";
                    String strtxtvar = "";

                    strtxtvar = Et_CIDatalayerID.getText().toString().trim();

                    if (strtxtvar.length() == 3) {
                        String strlast = strtxtvar.substring(strtxtvar.length() - 2);
                        String strfirst = strtxtvar.substring(0, 1);
                        String strFinal = "0" + strfirst + strlast;
                        command = "2E" + strFinal + Et_CIBuffer.getText().toString();

                        packet_size = command.length() / 2;
                        Total_Size_of_Packet = "0" + packet_size;

                        //Check Sum Calculation for CI Write
                        calchecksumvalues[0] = Integer.parseInt(packet_header.substring(0, 2), 16);
                        calchecksumvalues[1] = Integer.parseInt(packet_header.substring(2, 4), 16);
                        calchecksumvalues[2] = Integer.parseInt(packet_header.substring(4, 6), 16);//First three byte common header
                        calchecksumvalues[3] = Integer.parseInt(Total_Size_of_Packet, 16);//packetsize
                        calchecksumvalues[4] = Integer.parseInt("2E", 16);
                        calchecksumvalues[5] = Integer.parseInt(Et_CIDatalayerID.getText().toString().substring(0, 1), 16);
                        calchecksumvalues[6] = Integer.parseInt(Et_CIDatalayerID.getText().toString().substring(1, 3), 16);
                        calchecksumvalues[7] = Integer.parseInt(Et_CIBuffer.getText().toString(), 16);
                    } else if (strtxtvar.length() == 4) {

                        String strlast = strtxtvar.substring(strtxtvar.length() - 2);
                        String strfirst = strtxtvar.substring(0, 2);
                        String strFinal = strfirst + strlast;
                        command = "2E" + strFinal + Et_CIBuffer.getText().toString();

                        packet_size = command.length() / 2;
                        Total_Size_of_Packet = "0" + packet_size;

                        //Check Sum Calculation for CI Write
                        calchecksumvalues[0] = Integer.parseInt(packet_header.substring(0, 2), 16);
                        calchecksumvalues[1] = Integer.parseInt(packet_header.substring(2, 4), 16);
                        calchecksumvalues[2] = Integer.parseInt(packet_header.substring(4, 6), 16);//First three byte common header
                        calchecksumvalues[3] = Integer.parseInt(Total_Size_of_Packet, 16);//packetsize
                        calchecksumvalues[4] = Integer.parseInt("2E", 16);
                        calchecksumvalues[5] = Integer.parseInt(Et_CIDatalayerID.getText().toString().substring(0, 2), 16);
                        calchecksumvalues[6] = Integer.parseInt(Et_CIDatalayerID.getText().toString().substring(2, 4), 16);
                        calchecksumvalues[7] = Integer.parseInt(Et_CIBuffer.getText().toString(), 16);

                    } else {
                        command = "2E00" + Et_CIDatalayerID.getText().toString() + Et_CIBuffer.getText().toString();

                        packet_size = command.length() / 2;
                        Total_Size_of_Packet = "0" + packet_size;

                        //Check Sum Calculation for CI Write
                        calchecksumvalues[0] = Integer.parseInt(packet_header.substring(0, 2), 16);
                        calchecksumvalues[1] = Integer.parseInt(packet_header.substring(2, 4), 16);
                        calchecksumvalues[2] = Integer.parseInt(packet_header.substring(4, 6), 16);//First three byte common header
                        calchecksumvalues[3] = Integer.parseInt(Total_Size_of_Packet, 16);//packetsize
                        calchecksumvalues[4] = Integer.parseInt("2E", 16);
                        calchecksumvalues[5] = Integer.parseInt("00", 16);
                        calchecksumvalues[6] = Integer.parseInt(Et_CIDatalayerID.getText().toString().substring(0, 2), 16);
                        calchecksumvalues[7] = Integer.parseInt(Et_CIBuffer.getText().toString(), 16);
                    }

                    int isum = 0, isum2;
                    for (int i = 0; i < calchecksumvalues.length; i++) {
                        int isum1 = calchecksumvalues[i];
                        isum += isum1;
                    }
                    isum2 = isum % 256;

                    String buffervalue = Et_CIBuffer.getText().toString();
                    String checksumdata = String.valueOf(isum2);
                    Log.d("Checksum value", checksumdata);
                    if (checksumdata.length() == 1) {
                        checksumdata = (isum2 < 10 ? "0" : "") + isum2;
                    }

                    String hex_checksumdata = Integer.toHexString(Integer.parseInt(checksumdata));

                    if (hex_checksumdata.length() == 1) {
                        hex_checksumdata = "0" + hex_checksumdata;
                    }

                    //packet
                    Final_packecttosend = packet_header + Total_Size_of_Packet + command + hex_checksumdata;


                    if (TextUtils.isEmpty(Final_packecttosend)) {
                        return;
                    }

                    //Toast.makeText(getActivity(), "22222", Toast.LENGTH_SHORT).show();

                    fnBtn_ciwrite_BLEWrite();

                } catch (Exception e) {
                    Log.e("Exception :", e.toString());
                }
            }
        });

        Button Btn_ciread = view.findViewById(R.id.Btn_ciread);
        Btn_ciread.setText(getActivity().getString(R.string.read));
        Btn_ciread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Read = true;

                    String packet_header = "8060f1";

                    int packet_size = 0;
                    String Total_Size_of_Packet = "";

                    String command = "";

                    String strtxtvar = Et_CIDatalayerID.getText().toString().trim();

                    if (strtxtvar.length() == 3) {
                        String strlast = strtxtvar.substring(strtxtvar.length() - 2);
                        String strfirst = strtxtvar.substring(0, 1);
                        String strFinal = "0" + strfirst + strlast;
                        command = "22" + strFinal;

                        packet_size = command.length() / 2;
                        Total_Size_of_Packet = "0" + packet_size;

                        //Check Sum Calculation for CI Read
                        calchecksumvalues[0] = Integer.parseInt(packet_header.substring(0, 2), 16);
                        calchecksumvalues[1] = Integer.parseInt(packet_header.substring(2, 4), 16);
                        calchecksumvalues[2] = Integer.parseInt(packet_header.substring(4, 6), 16);//First three byte common header
                        calchecksumvalues[3] = Integer.parseInt(Total_Size_of_Packet, 16);//packetsize
                        calchecksumvalues[4] = Integer.parseInt("2E", 16);
                        calchecksumvalues[5] = Integer.parseInt(Et_CIDatalayerID.getText().toString().substring(0, 1), 16);
                        calchecksumvalues[6] = Integer.parseInt(Et_CIDatalayerID.getText().toString().substring(1, 3), 16);
                        calchecksumvalues[7] = Integer.parseInt(Et_CIBuffer.getText().toString(), 16);
                    } else if (strtxtvar.length() == 4) {

                        String strlast = strtxtvar.substring(strtxtvar.length() - 2);
                        String strfirst = strtxtvar.substring(0, 2);
                        String strFinal = strfirst + strlast;
                        command = "22" + strFinal;

                        packet_size = command.length() / 2;
                        Total_Size_of_Packet = "0" + packet_size;

                        //Check Sum Calculation for CI Read
                        calchecksumvalues[0] = Integer.parseInt(packet_header.substring(0, 2), 16);
                        calchecksumvalues[1] = Integer.parseInt(packet_header.substring(2, 4), 16);
                        calchecksumvalues[2] = Integer.parseInt(packet_header.substring(4, 6), 16);//First three byte common header
                        calchecksumvalues[3] = Integer.parseInt(Total_Size_of_Packet, 16);//packetsize
                        calchecksumvalues[4] = Integer.parseInt("2E", 16);
                        calchecksumvalues[5] = Integer.parseInt(Et_CIDatalayerID.getText().toString().substring(0, 2), 16);
                        calchecksumvalues[6] = Integer.parseInt(Et_CIDatalayerID.getText().toString().substring(2, 4), 16);
                        calchecksumvalues[7] = Integer.parseInt(Et_CIBuffer.getText().toString(), 16);

                    } else {
                        command = "2200" + Et_CIDatalayerID.getText().toString();

                        packet_size = command.length() / 2;
                        Total_Size_of_Packet = "0" + packet_size;

                        //Check Sum Calculation for CI Read
                        calchecksumvalues[0] = Integer.parseInt(packet_header.substring(0, 2), 16);
                        calchecksumvalues[1] = Integer.parseInt(packet_header.substring(2, 4), 16);
                        calchecksumvalues[2] = Integer.parseInt(packet_header.substring(4, 6), 16);//First three byte common header
                        calchecksumvalues[3] = Integer.parseInt(Total_Size_of_Packet, 16);//packetsize
                        calchecksumvalues[4] = Integer.parseInt("2E", 16);
                        calchecksumvalues[5] = Integer.parseInt("00", 16);
                        calchecksumvalues[6] = Integer.parseInt(Et_CIDatalayerID.getText().toString().substring(0, 2), 16);
                        calchecksumvalues[7] = Integer.parseInt(Et_CIBuffer.getText().toString(), 16);
                    }

                    int isum = 0, isum2;
                    for (int i = 0; i < calchecksumvalues.length; i++) {
                        int isum1 = calchecksumvalues[i];
                        isum += isum1;
                    }
                    isum2 = isum % 256;

                    String buffervalue = Et_CIBuffer.getText().toString();
                    String checksumdata = String.valueOf(isum2);
                    Log.d("Checksum value", checksumdata);
                    if (checksumdata.length() == 1) {
                        checksumdata = (isum2 < 10 ? "0" : "") + isum2;
                    }

                    String hex_checksumdata = Integer.toHexString(Integer.parseInt(checksumdata));

                    if (hex_checksumdata.length() == 1) {
                        hex_checksumdata = "0" + hex_checksumdata;
                    }

                    //packet
                    Final_packecttosend = packet_header + Total_Size_of_Packet + command + hex_checksumdata;

                    if (TextUtils.isEmpty(Final_packecttosend)) {

                        return;
                    }

                    fnBtn_ciread_BLEWrite();

                    BleManager.getInstance().read(
                            bleDevice,
                            characteristic.getService().getUuid().toString(),
                            characteristic.getUuid().toString(),
                            new BleReadCallback() {
                                @Override
                                public void onReadSuccess(final byte[] data) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Btnciread = true;
                                            fnReadValue(txt, data);
                                        }
                                    });
                                }

                                @Override
                                public void onReadFailure(final BleException exception) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            //addText(txt, exception.toString());
                                        }
                                    });
                                }
                            });

                } catch (Exception e) {
                    Log.e("Exception :", e.toString());
                }
            }
        });


        //Read for Data by Common Identifier
        MA_Btnread = view.findViewById(R.id.MA_Btnread);
        MA_Btnread.setText(getActivity().getString(R.string.read));
        MA_Btnread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Read = true;
                    String packet_header = "8060f10623";
                    String packet_size = Et_MA_Size.getText().toString();
                    if (packet_size.length() == 1) {
                        packet_size = "0" + Et_MA_Size.getText().toString();
                    }

                    String command = "";
                    if (Et_MA_Address.length() == 0) {
                        command = "00000000";
                    } else if (Et_MA_Address.length() == 2) {
                        command = "000000" + Et_MA_Address.getText().toString();
                    } else if (Et_MA_Address.length() == 4) {
                        command = "0000" + Et_MA_Address.getText().toString();
                    } else if (Et_MA_Address.length() == 6) {
                        command = "00" + Et_MA_Address.getText().toString();
                    } else if (Et_MA_Address.length() == 8) {
                        command = Et_MA_Address.getText().toString();
                    } else if (Et_MA_Address.length() > 8) {
                        command = Et_MA_Address.getText().toString().substring(0, 8);
                    }


                    calchecksumvalues[0] = Integer.parseInt(packet_header.substring(0, 2), 16);
                    calchecksumvalues[1] = Integer.parseInt(packet_header.substring(2, 4), 16);
                    calchecksumvalues[2] = Integer.parseInt(packet_header.substring(4, 6), 16);
                    calchecksumvalues[3] = Integer.parseInt(packet_header.substring(6, 8), 16);
                    calchecksumvalues[4] = Integer.parseInt(packet_header.substring(8, 10), 16);
                    calchecksumvalues[5] = Integer.parseInt(command.substring(0, 2), 16);
                    calchecksumvalues[6] = Integer.parseInt(command.substring(2, 4), 16);
                    calchecksumvalues[7] = Integer.parseInt(command.substring(4, 6), 16);
                    calchecksumvalues[8] = Integer.parseInt(command.substring(4, 6), 16);
                    calchecksumvalues[9] = Integer.parseInt(packet_size, 16);

                    //Check Sum Calculation for CI Write

                    int isum = 0, isum2;
                    for (int i = 0; i < calchecksumvalues.length; i++) {
                        int isum1 = calchecksumvalues[i];
                        isum += isum1;
                    }
                    isum2 = isum % 256;

                    String buffervalue = Et_CIBuffer.getText().toString();
                    String checksumdata = String.valueOf(isum2);
                    Log.d("Checksum value", checksumdata);
                    if (checksumdata.length() == 1) {
                        checksumdata = (isum2 < 10 ? "0" : "") + isum2;
                    }

                    String hex_checksumdata = Integer.toHexString(Integer.parseInt(checksumdata));

                    if (hex_checksumdata.length() == 1) {
                        hex_checksumdata = "0" + hex_checksumdata;
                    }

                    //packet
                    Final_packecttosend = packet_header + command + packet_size + hex_checksumdata;

                    if (TextUtils.isEmpty(Final_packecttosend)) {
                        return;
                    }

                    fnMA_Btnread_BLEWrite();
                } catch (Exception e) {
                    Log.e("Exception :", e.toString());
                }
            }
        });


        //Write for Data by Memory Address
        MA_Btnwrite = view.findViewById(R.id.MA_Btnwrite);
        MA_Btnwrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    Read = true;
                    String packet_header = "8060f10623";
                    String packet_size = Et_MA_Size.getText().toString();
                    if (packet_size.length() == 1) {
                        packet_size = "0" + Et_MA_Size.getText().toString();
                    }


                    String command = "2E" + Et_MA_Address.getText().toString() + Et_MA_Buffer.getText().toString();


                    calchecksumvalues[0] = Integer.parseInt(packet_header.substring(0, 2), 16);
                    calchecksumvalues[1] = Integer.parseInt(packet_header.substring(2, 4), 16);
                    calchecksumvalues[2] = Integer.parseInt(packet_header.substring(4, 6), 16);
                    calchecksumvalues[3] = Integer.parseInt(packet_size, 16);
                    calchecksumvalues[4] = Integer.parseInt(command.substring(0, 2), 16);
                    calchecksumvalues[5] = Integer.parseInt(command.substring(2, 4), 16);
                    calchecksumvalues[6] = Integer.parseInt(command.substring(4, 6), 16);
                    calchecksumvalues[7] = Integer.parseInt(command.substring(6, 8), 16);

                    //Check Sum Calculation for CI Write


                    int isum = 0, isum2;
                    for (int i = 0; i < calchecksumvalues.length; i++) {
                        int isum1 = calchecksumvalues[i];
                        isum += isum1;
                    }
                    isum2 = isum % 256;

                    String buffervalue = Et_CIBuffer.getText().toString();
                    String checksumdata = String.valueOf(isum2);
                    Log.d("Checksum value", checksumdata);
                    if (checksumdata.length() == 1) {
                        checksumdata = (isum2 < 10 ? "0" : "") + isum2;
                    }

                    String hex_checksumdata = Integer.toHexString(Integer.parseInt(checksumdata));

                    if (hex_checksumdata.length() == 1) {
                        hex_checksumdata = "0" + hex_checksumdata;
                    }

                    //packet
                    Final_packecttosend = packet_header + packet_size + command + hex_checksumdata;

                    if (TextUtils.isEmpty(Final_packecttosend)) {
                        return;
                    }

                    fnMA_Btnwrite_BLEWrite();
                } catch (Exception e) {
                    Log.e("Exception :", e.toString());
                }
            }
        });


        //send for Data by Memory Address
        btn.setText(getActivity().

                        getString(R.string.write));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Read = true;
                hex = Et_Send.getText().toString();
                if (TextUtils.isEmpty(hex)) {
                    return;
                }
                //"a3ce2315-3634-4e2c-89a2-ae9d0442d02f","0000fff2-0000-1000-8000-00805f9b34fb1",

                fnbtn_BLEWrite();
            }
        });

        layout_container.addView(view);
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(getActivity(), "Write External Storage permission allows us to create files. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }


    public void fnReadValue(TextView txt, byte[] data) {
        addText(txt, "Rx: " + HexUtil.formatHexString(data, true).substring(0, Math.min(HexUtil.formatHexString(data, true).length(), 29)));

        Log.d("receive data", HexUtil.formatHexString(data, true));

        String Receive_hex = (HexUtil.formatHexString(data, true).substring(0, Math.min(HexUtil.formatHexString(data, true).length(), 29)));

        //Toast.makeText(getActivity(), "Receive_hex=" + Receive_hex, Toast.LENGTH_SHORT).show();

        //Enable scroll view
        if (Receive_hex.contains("80 60 f1 03 c1 00 12 a7") || Receive_hex.contains("80 60 f1 04 c1 00 12 a7")) {
            Start.setBackgroundColor(Start.getContext().getResources().getColor(R.color.Green));
            stop.setBackground(getResources().getDrawable(R.drawable.cellshapbutton1));
            scrollView.setVisibility(View.VISIBLE);
        }

        if (Receive_hex.contains("80 60 f1 03 c2 96") || Receive_hex.contains("80 60 f1 04 c2 96")) {
            stop.setBackgroundColor(stop.getContext().getResources().getColor(R.color.Red));
            Start.setBackground(getResources().getDrawable(R.drawable.cellshapbutton1));
            scrollView.setVisibility(View.GONE);
        }
    }

    private void runOnUiThread(Runnable runnable) {
        if (isAdded() && getActivity() != null)
            getActivity().runOnUiThread(runnable);
    }

    private void addText(TextView textView, String content) {
        textView.append(content);
        textView.append("\n");
        int offset = textView.getLineCount() * textView.getLineHeight();
        if (offset > textView.getHeight()) {
            textView.scrollTo(0, offset - textView.getHeight());
        }
    }

    private byte[] CalCheckSum(byte[] BufferToCal) {
        int index = 0;
        int isum = 0;
        byte bmodule = 0;
        for (; index < BufferToCal.length - 1; index++) {
            isum += BufferToCal[index];
        }
        bmodule = (byte) (isum % 256);
        BufferToCal[index] = bmodule;
        return BufferToCal;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }

        return data;
    }

    public void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public void fnBtn_ciwrite_BLEWrite()
    {
        BleManager.getInstance().write(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                "a3ce2315-3634-4e2c-89a2-ae9d0442d02f",
                HexUtil.hexStringToBytes(Final_packecttosend),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                        //Toast.makeText(getActivity(), "33333", Toast.LENGTH_SHORT).show();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addText(txt, "Tx: " + HexUtil.formatHexString(justWrite, true));
                            }
                        });
                    }

                    @Override
                    public void onWriteFailure(final BleException exception) {
                        //Toast.makeText(getActivity(), exception.toString(), Toast.LENGTH_SHORT).show();

                        runOnUiThread(new Runnable() {
                            @SuppressLint("ResourceAsColor")
                            @Override
                            public void run() {
                                fnBtn_ciwrite_BLEWrite();
                            }
                        });
                    }
                });
    }

    public void fnBtn_writemodules_BLEWrite()
    {
        //start write
        BleManager.getInstance().write(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                "a3ce2315-3634-4e2c-89a2-ae9d0442d02f",
                HexUtil.hexStringToBytes(hex),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                        Toast.makeText(getActivity(), "Success123", Toast.LENGTH_SHORT).show();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addText(txt, "Tx: " + HexUtil.formatHexString(justWrite, true));
                            }
                        });
                    }

                    @Override
                    public void onWriteFailure(final BleException exception) {
                        Toast.makeText(getActivity(), "Error123", Toast.LENGTH_SHORT).show();
                        runOnUiThread(new Runnable() {
                            @SuppressLint("ResourceAsColor")
                            @Override
                            public void run() {
                                //  addText(txt, exception.toString());
                                fnBtn_writemodules_BLEWrite();
                            }
                        });
                    }
                });
    }

    public void fnMA_Btnwrite_BLEWrite()
    {
        BleManager.getInstance().write(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                "a3ce2315-3634-4e2c-89a2-ae9d0442d02f",
                HexUtil.hexStringToBytes(Final_packecttosend),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addText(txt, "Tx: " + HexUtil.formatHexString(justWrite, true));
                            }
                        });

                    }

                    @Override
                    public void onWriteFailure(final BleException exception) {
                        runOnUiThread(new Runnable() {
                            @SuppressLint("ResourceAsColor")
                            @Override
                            public void run() {
                                fnMA_Btnwrite_BLEWrite();
                            }
                        });
                    }
                });
    }

    public void fnBtn_ciread_BLEWrite()
    {
        BleManager.getInstance().write(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                "a3ce2315-3634-4e2c-89a2-ae9d0442d02f",
                HexUtil.hexStringToBytes(Final_packecttosend),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addText(txt, "Tx: " + HexUtil.formatHexString(justWrite, true));
                            }
                        });
                    }

                    @Override
                    public void onWriteFailure(final BleException exception) {
                        runOnUiThread(new Runnable() {
                            @SuppressLint("ResourceAsColor")
                            @Override
                            public void run() {
                                fnBtn_ciread_BLEWrite();
                            }
                        });
                    }
                });
    }

    public void fnBtn_Start_BLEWrite() {
        BleManager.getInstance().write(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                "a3ce2315-3634-4e2c-89a2-ae9d0442d02f",
                HexUtil.hexStringToBytes(hex),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addText(txt, "Tx: " + HexUtil.formatHexString(justWrite, true));
                            }
                        });

                    }

                    @Override
                    public void onWriteFailure(final BleException exception) {
                        runOnUiThread(new Runnable() {
                            @SuppressLint("ResourceAsColor")
                            @Override
                            public void run() {
                                //addText(txt, exception.toString());
                                fnBtn_Start_BLEWrite();
                            }
                        });
                    }
                });
    }

    public void fnBtn_Stop_BLEWrite()
    {
        BleManager.getInstance().write(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                "a3ce2315-3634-4e2c-89a2-ae9d0442d02f",
                HexUtil.hexStringToBytes(hex),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addText(txt, "Tx: " + HexUtil.formatHexString(justWrite, true));
                            }
                        });

                    }

                    @Override
                    public void onWriteFailure(final BleException exception) {
                        runOnUiThread(new Runnable() {
                            @SuppressLint("ResourceAsColor")
                            @Override
                            public void run() {
                                fnBtn_Stop_BLEWrite();
                            }
                        });
                    }
                });
    }

    public void fnMA_Btnread_BLEWrite()
    {
        BleManager.getInstance().write(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                "a3ce2315-3634-4e2c-89a2-ae9d0442d02f",
                HexUtil.hexStringToBytes(Final_packecttosend),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addText(txt, "Tx: " + HexUtil.formatHexString(justWrite, true));
                            }
                        });

                    }

                    @Override
                    public void onWriteFailure(final BleException exception) {
                        runOnUiThread(new Runnable() {
                            @SuppressLint("ResourceAsColor")
                            @Override
                            public void run() {
                                // addText(txt, exception.toString());
                                fnMA_Btnread_BLEWrite();
                            }
                        });
                    }
                });
    }

    public void fnbtn_BLEWrite()
    {
        BleManager.getInstance().write(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                "a3ce2315-3634-4e2c-89a2-ae9d0442d02f",
                HexUtil.hexStringToBytes(hex),
                new BleWriteCallback() {

                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addText(txt, "Tx: " + HexUtil.formatHexString(justWrite, true));
                            }
                        });
                    }

                    @Override
                    public void onWriteFailure(final BleException exception) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // addText(txt, exception.toString());
                                fnbtn_BLEWrite();
                            }
                        });
                    }
                });
    }

    public void fnThread_BLERead()
    {
        BleManager.getInstance().read(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                new BleReadCallback() {
                    @Override
                    public void onReadSuccess(final byte[] data) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Btnciread = false;
                                fnReadValue(txt, data);
                            }
                        });
                    }

                    @Override
                    public void onReadFailure(final BleException exception) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //addText(txt, exception.toString());
                                //Toast.makeText(getActivity(), "exception=" + exception.toString(), Toast.LENGTH_SHORT).show();
                                fnThread_BLERead();
                            }
                        });
                    }
                });
    }
}
package com.codeworks.ckh.filemanager;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by CKH on 5/21/2016.
 */
public class DirectoryListFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_BASE_DIRECTORY = "base_directory";
    private String currentDirectory;
    private ArrayList<File> dir;
    private ListviewDirectoryAdapter listAdapter;
    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static DirectoryListFragment newInstance(String directory) {
        DirectoryListFragment fragment = new DirectoryListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_BASE_DIRECTORY, directory);
        fragment.setArguments(args);
        return fragment;
    }

    public DirectoryListFragment() {
                    }



                    @Override
                    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
                        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
                        Bundle bundle = this.getArguments();
                        try{
                            currentDirectory = bundle.getString(ARG_BASE_DIRECTORY);
                        }catch ( NullPointerException e )
                        {
                            currentDirectory = "/";
                        }
                        Log.i("DirectoryFragment", "OnCreateView_CurrentDirectory:" + currentDirectory);
                        File directory = new File( currentDirectory );
                        final File[] directoryList = directory.listFiles();

                        dir = new ArrayList<File>( Arrays.asList(directoryList) );
                        Collections.sort(dir, new FileSorter());
                        ListView list = (ListView)rootView.findViewById(R.id.listViewDirectory );
                        listAdapter = new ListviewDirectoryAdapter(getActivity(), dir);
                        list.setAdapter( listAdapter );
                        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                //here you can use the position to determine what checkbox to check
                                //this assumes that you have an array of your checkboxes as well. called checkbox
                                File selection = dir.get(position);
                                if (selection.isDirectory()) {
                                    String nextDirectory = dir.get(position).getAbsolutePath();
                                    if (currentDirectory == "/") {
                        nextDirectory = currentDirectory + dir.get(position);
                    }
                    ((MainActivity) getActivity()).changeDirectory(nextDirectory);
                } else {
                    try {
                        openFile(getActivity(), selection);
                    } catch (IOException e) {
                        Toast.makeText(getActivity(), "Unable to open file", Toast.LENGTH_SHORT).show();
                    } catch (ActivityNotFoundException e1) {
                        Toast.makeText(getActivity(), "No activity found for this file type ", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        registerForContextMenu(list);
        list.setOnCreateContextMenuListener(this);
        return rootView;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
        menu.setHeaderTitle(dir.get(info.position).getName());
        menu.add("Copy");
        menu.add("Past");
        menu.add("Delete");

        for (int i=0;i<menu.size();i++)
        {
            Log.d("onCreateContextMenu", "adding actions ");
            menu.getItem(i).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuItem.getMenuInfo();

                    Log.d("CONTEXTMENULISTENER", "selected context menu");
                    Toast.makeText(getActivity(), menuItem.getTitle(), Toast.LENGTH_SHORT ).show();

                    if( menuItem.getTitle() == "Delete")
                    {
                        Log.d( "ClickedDelete", "going to delete file");
                        dir.get( info.position ).delete();

                        File directory = new File( currentDirectory );
                        final File[] directoryList = directory.listFiles();

                        dir = new ArrayList<File>( Arrays.asList(directoryList) );
                        listAdapter.updateFileList(dir);
                    }
                    return false;
                }
            });
        }
        /*String[] menuItems = getResources().getStringArray(R.array.menu);
        for (int i = 0; i<menuItems.length; i++) {
            menu.add(Menu.NONE, i, i, menuItems[i]);
        }*/
    }


    private class FileSorter implements Comparator<File>
    {
        @Override
        public boolean equals(Object o) {
            return false;
        }

        @Override
        public int compare(File file, File t1) {
            int result = 0;
            if( file.isDirectory())
            {
                if( t1.isDirectory() )
                {
                    result = file.getName().compareTo( t1.getName() );
                }
                else
                {
                    result = -1 ;
                }
            }
            else if( t1.isDirectory() )
            {
                result = 1;
            }
            else
            {
                result = file.getName().compareTo( t1.getName() );
            }
            return result;
        }
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getString(ARG_BASE_DIRECTORY));
    }

    public String getFileSize( File file)
    {
        String fileSize = "";
        long sizeInKB = (file.length() / 1024 );

        if( sizeInKB > 1024 )
        {
            fileSize =  ( sizeInKB / 1024 )+ "MB";
        }
        else
        {
            fileSize =  ( sizeInKB ) + "KB";
        }

        return fileSize;
    }
    public static void openFile(Context context, File url) throws IOException {
        // Create URI
        File file=url;
        Uri uri = Uri.fromFile(file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        // Check what kind of file you are trying to open, by comparing the url with extensions.
        // When the if condition is matched, plugin sets the correct intent (mime) type,
        // so Android knew what application to use to open the file
        if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
            // Word document
            intent.setDataAndType(uri, "application/msword");
        } else if(url.toString().contains(".pdf")) {
            // PDF file
            intent.setDataAndType(uri, "application/pdf");
        } else if(url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        } else if(url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
            // Excel file
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        } else if(url.toString().contains(".zip") || url.toString().contains(".rar")) {
            // WAV audio file
            intent.setDataAndType(uri, "application/x-wav");
        } else if(url.toString().contains(".rtf")) {
            // RTF file
            intent.setDataAndType(uri, "application/rtf");
        } else if(url.toString().contains(".wav") || url.toString().contains(".mp3")) {
            // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav");
        } else if(url.toString().contains(".gif")) {
            // GIF file
            intent.setDataAndType(uri, "image/gif");
        } else if(url.toString().contains(".jpg") || url.toString().contains(".jpeg") || url.toString().contains(".png")) {
            // JPG file
            intent.setDataAndType(uri, "image/jpeg");
        } else if(url.toString().contains(".txt")) {
            // Text file
            intent.setDataAndType(uri, "text/plain");
        } else if(url.toString().contains(".3gp") || url.toString().contains(".mpg") || url.toString().contains(".mpeg") || url.toString().contains(".mpe") || url.toString().contains(".mp4") || url.toString().contains(".avi")) {
            // Video files
            intent.setDataAndType(uri, "video/*");
        } else {
            //if you want you can also define the intent type for any other file

            //additionally use else clause below, to manage other unknown extensions
            //in this case, Android will show all applications installed on the device
            //so you can choose which application to use
            intent.setDataAndType(uri, "*/*");
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public class ListviewDirectoryAdapter extends BaseAdapter {
        private ArrayList<File> listDirectory;

        private LayoutInflater mInflater;

        public ListviewDirectoryAdapter(Context directoryFragment, ArrayList<File> directory) {
            listDirectory = directory;
            mInflater = LayoutInflater.from(directoryFragment);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return listDirectory.size();
        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return listDirectory.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }

        public void updateFileList( ArrayList<File> files )
        {
            listDirectory = files;

            this.notifyDataSetChanged();
        }


        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.directory_list, null);
                holder = new ViewHolder();
                holder.icon = (ImageView) convertView.findViewById(R.id.imageViewFileType);
                holder.directoryName = (TextView) convertView.findViewById(R.id.textViewDirectoryName);
                holder.fileSize = ( TextView ) convertView.findViewById( R.id.textViewDirectoryType );
                holder.lastEdit = ( TextView ) convertView.findViewById( R.id.textViewEditDate );
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            File dir = listDirectory.get(position);

            if (dir != null && dir.isFile()) {
                holder.icon.setImageResource(R.drawable.file_icon_light);
                holder.fileSize.setText( getFileSize( dir ));
            } else {
                holder.icon.setImageResource(R.drawable.folder_icon_light);
                holder.fileSize.setText(R.string.label_directory);
            }
            Date lastedit = new Date( dir.lastModified() );
            Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            holder.lastEdit.setText( formatter.format( lastedit ));
            holder.directoryName.setText(listDirectory.get(position).getName());

            return convertView;
        }


        class ViewHolder {
            TextView fileSize, directoryName, lastEdit;
            ImageView icon;
        }


    }
}

package com.example.managemeister.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.managemeister.R
import com.example.managemeister.firebase.FirestoreClass
import com.example.managemeister.models.Board
import com.example.managemeister.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_create_board.*
import java.io.IOException

@Suppress("DEPRECATION")
class CreateBoardActivity : BaseActivity() {
    private var mSelectedImageFileUri: Uri? = null
    private lateinit var mUserName: String
    private var mBoardImageURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_board)

        setupActionBar()

        if (intent.hasExtra(Constants.NAME)) {
            mUserName = intent.getStringExtra(Constants.NAME)!!
        }

        iv_board_image.setOnClickListener { view ->

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                Constants.showImageChooser(this@CreateBoardActivity)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }
        btn_create.setOnClickListener {

            // Here if the image is not selected then update the other details of user.
            if (mSelectedImageFileUri != null) {

                uploadBoardImage()
            } else {

                showProgressDialog(resources.getString(R.string.please_wait))

                // Call a function to update create a board.
                createBoard()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            //If permission is granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this@CreateBoardActivity)
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(
                    this,
                    "Oops, you just denied the permission for storage. You can also allow it from settings.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
            && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
            && data!!.data != null
        ) {
            mSelectedImageFileUri = data.data

            try {
                // Load the board image in the ImageView.
                Glide
                    .with(this@CreateBoardActivity)
                    .load(Uri.parse(mSelectedImageFileUri.toString())) // URI of the image
                    .centerCrop() // Scale type of the image.
                    .placeholder(R.drawable.ic_user_place_holder) // A default place holder
                    .into(iv_board_image) // the view in which the image will be loaded.
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun setupActionBar() {

        setSupportActionBar(toolbar_create_board_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_colour_back_24dp)
        }

        toolbar_create_board_activity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun uploadBoardImage() {
        showProgressDialog(resources.getString(R.string.please_wait))

        //getting the storage reference
        val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
            "BOARD_IMAGE" + System.currentTimeMillis() + "."
                    + Constants.getFileExtension(this@CreateBoardActivity, mSelectedImageFileUri)
        )

        //adding the file to reference
        sRef.putFile(mSelectedImageFileUri!!)
            .addOnSuccessListener { taskSnapshot ->
                // The image upload is success
                Log.e(
                    "Firebase Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                // Get the downloadable url from the task snapshot
                taskSnapshot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { uri ->
                        Log.e("Downloadable Image URL", uri.toString())

                        // assign the image url to the variable.
                        mBoardImageURL = uri.toString()

                        // Call a function to create the board.
                        createBoard()
                    }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this@CreateBoardActivity,
                    exception.message,
                    Toast.LENGTH_LONG
                ).show()

                hideProgressDialog()
            }
    }

    private fun createBoard() {

        //  A list is created to add the assigned members.
        //  This can be modified later on as of now the user itself will be the member of the board.
        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(getCurrentUserID()) // adding the current user id.

        // Creating the instance of the Board and adding the values as per parameters.
        val board = Board(
            et_board_name.text.toString(),
            mBoardImageURL,
            mUserName,
            assignedUsersArrayList
        )

        FirestoreClass().createBoard(this@CreateBoardActivity, board)
    }

    fun boardCreatedSuccessfully() {

        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
}
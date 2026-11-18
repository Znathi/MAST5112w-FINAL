
package com.example.cookmenu

import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


data class MenuItem(
    val name: String,
    val description: String,
    val course: String,
    val price: Double
)

class MainActivity : AppCompatActivity() {


    private val menuItems = mutableListOf<MenuItem>()

    private var editingIndex = -1

    private lateinit var dishName: EditText
    private lateinit var description: EditText
    private lateinit var course: Spinner
    private lateinit var price: EditText
    private lateinit var addButton: Button
    private lateinit var menuList: LinearLayout
    private lateinit var emptyText: TextView
    private lateinit var searchInput: EditText
    private lateinit var filterSpinner: Spinner
    private lateinit var statsText: TextView
    private lateinit var formTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }


        dishName = findViewById(R.id.etDish)
        description = findViewById(R.id.etDescription)
        course = findViewById(R.id.spCourse)
        price = findViewById(R.id.etPrice)
        addButton = findViewById(R.id.btnAdd)
        menuList = findViewById(R.id.menuItemsContainer)
        emptyText = findViewById(R.id.tvEmpty)
        searchInput = findViewById(R.id.etSearch)
        filterSpinner = findViewById(R.id.spFilter)
        statsText = findViewById(R.id.tvStats)
        formTitle = findViewById(R.id.tvFormTitle)


        val courses = arrayOf(
            "Select Course",
            "Starter",
            "Main Course",
            "Dessert",
            "Drink"
        )

        val courseAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            courses
        )

        courseAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        course.adapter = courseAdapter


        val filters = arrayOf(
            "All Courses",
            "Starter",
            "Main Course",
            "Dessert",
            "Drink"
        )

        val filterAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            filters
        )

        filterAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        filterSpinner.adapter = filterAdapter


        addButton.setOnClickListener {

            val name = dishName.text.toString().trim()
            val details = description.text.toString().trim()
            val selectedCourse = course.selectedItem.toString()
            val priceText = price.text.toString().trim()

            // Validate the fields
            if (name.isEmpty() || details.isEmpty() ||
                priceText.isEmpty() ||
                selectedCourse == "Select Course"
            ) {
                Toast.makeText(
                    this,
                    "Please complete all fields",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val priceValue = priceText.toDoubleOrNull()

            if (priceValue == null ||
                !priceValue.isFinite() ||
                priceValue < 0
            ) {
                price.error = "Enter a valid price"
                return@setOnClickListener
            }

            val item = MenuItem(
                name,
                details,
                selectedCourse,
                priceValue
            )

            if (editingIndex == -1) {

                // Add a new dish
                menuItems.add(item)

                Toast.makeText(
                    this,
                    "Menu item added!",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                // Update an existing dish
                menuItems[editingIndex] = item

                Toast.makeText(
                    this,
                    "Menu item updated!",
                    Toast.LENGTH_SHORT
                ).show()
            }

            clearForm()
            renderMenu()
        }


        searchInput.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                renderMenu()
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        filterSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    renderMenu()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }


        findViewById<Button>(R.id.btnClear).setOnClickListener {

            searchInput.text.clear()
            filterSpinner.setSelection(0)

            Toast.makeText(
                this,
                "Search and filter cleared",
                Toast.LENGTH_SHORT
            ).show()

            renderMenu()
        }

        renderMenu()
    }


    private fun clearForm() {

        dishName.text.clear()
        description.text.clear()
        price.text.clear()
        course.setSelection(0)

        editingIndex = -1

        addButton.text = "Add to menu"
        formTitle.text = "Add an Item"
    }


    private fun renderMenu() {

        // Keep the menu heading and empty message
        if (menuList.childCount > 2) {
            menuList.removeViews(
                2,
                menuList.childCount - 2
            )
        }

        val searchText = searchInput.text.toString().trim()

        val selectedFilter = filterSpinner.selectedItem?.toString()
            ?: "All Courses"

        // Search and filter the menu
        val visibleItems = menuItems.mapIndexed { index, item ->
            index to item
        }.filter { (_, item) ->

            val matchesSearch =
                item.name.contains(searchText, ignoreCase = true)

            val matchesCourse =
                selectedFilter == "All Courses" ||
                        item.course == selectedFilter

            matchesSearch && matchesCourse
        }

        // Show message when no items match
        if (visibleItems.isEmpty()) {

            emptyText.visibility = View.VISIBLE

            emptyText.text = if (menuItems.isEmpty()) {
                "No menu items yet."
            } else {
                "No matching menu items found."
            }

        } else {
            emptyText.visibility = View.GONE
        }


        visibleItems.forEach { (index, item) ->

            val card = LinearLayout(this)
            card.orientation = LinearLayout.VERTICAL
            card.setPadding(32, 24, 32, 24)

            val background = GradientDrawable()
            background.setColor(Color.WHITE)
            background.cornerRadius = 20f
            background.setStroke(2, Color.LTGRAY)

            card.background = background

            val title = TextView(this)

            title.text = "${item.name} - R%.2f".format(item.price)
            title.textSize = 18f
            title.setTextColor(Color.rgb(23, 76, 58))
            title.setTypeface(null, Typeface.BOLD)

            val details = TextView(this)

            details.text =
                "${item.description}\nCourse: ${item.course}"

            details.textSize = 14f
            details.setTextColor(Color.DKGRAY)


            val buttonLayout = LinearLayout(this)
            buttonLayout.orientation = LinearLayout.HORIZONTAL

            val editButton = Button(this)
            editButton.text = "Edit"

            val deleteButton = Button(this)
            deleteButton.text = "Delete"

            // Edit an existing dish
            editButton.setOnClickListener {
                editMenuItem(index)
            }


            deleteButton.setOnClickListener {

                menuItems.removeAt(index)

                if (editingIndex == index) {
                    clearForm()
                } else if (editingIndex > index) {
                    editingIndex--
                }

                Toast.makeText(
                    this,
                    "Menu item deleted!",
                    Toast.LENGTH_SHORT
                ).show()

                renderMenu()
            }

            buttonLayout.addView(
                editButton,
                LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            buttonLayout.addView(
                deleteButton,
                LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            card.addView(title)
            card.addView(details)
            card.addView(buttonLayout)

            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            params.bottomMargin = 16

            menuList.addView(card, params)
        }

        updateStatistics()
    }


    private fun editMenuItem(index: Int) {

        val item = menuItems[index]

        dishName.setText(item.name)
        description.setText(item.description)
        price.setText(item.price.toString())

        val coursePosition = when (item.course) {
            "Starter" -> 1
            "Main Course" -> 2
            "Dessert" -> 3
            "Drink" -> 4
            else -> 0
        }

        course.setSelection(coursePosition)

        editingIndex = index

        addButton.text = "Update Item"
        formTitle.text = "Edit Menu Item"

        Toast.makeText(
            this,
            "Edit the details and press Update Item",
            Toast.LENGTH_SHORT
        ).show()
    }


    private fun updateStatistics() {

        val totalItems = menuItems.size

        val averagePrice = if (totalItems > 0) {
            menuItems.sumOf { it.price } / totalItems
        } else {
            0.0
        }

        val starters = menuItems.count {
            it.course == "Starter"
        }

        val mainCourses = menuItems.count {
            it.course == "Main Course"
        }

        val desserts = menuItems.count {
            it.course == "Dessert"
        }

        val drinks = menuItems.count {
            it.course == "Drink"
        }

        statsText.text = """
            Total Menu Items: $totalItems

            Average Price: R%.2f

            Starters: $starters

            Main Courses: $mainCourses

            Desserts: $desserts

            Drinks: $drinks
        """.trimIndent().format(averagePrice)
    }
}

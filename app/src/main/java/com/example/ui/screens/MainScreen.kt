package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.CapacityStudy
import com.example.data.database.StudyProcess
import com.example.ui.theme.*
import com.example.ui.viewmodel.CapacityStudyViewModel
import com.example.ui.viewmodel.StopwatchState
import java.text.SimpleDateFormat
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import android.content.Intent
import android.net.Uri
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: CapacityStudyViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // ViewModel states
    val studies by viewModel.allStudies.collectAsStateWithLifecycle()
    val activeStudy by viewModel.activeStudy.collectAsStateWithLifecycle()
    val activeProcesses by viewModel.activeProcesses.collectAsStateWithLifecycle()
    val stopwatchState by viewModel.stopwatchState.collectAsStateWithLifecycle()
    
    // UI Dialog States
    var showStudyEditDialog by remember { mutableStateOf(false) }
    var showCreateStudyDialog by remember { mutableStateOf(false) }
    var showProcessEditDialog by remember { mutableStateOf<StudyProcess?>(null) }
    var showAuthorInfoDialog by remember { mutableStateOf(false) }

    // Tab state (0 = Calculations & Study, 1 = Visual Capacity Graph, 2 = Saved Studies History)
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Garments Capacity Study",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = ElegantDarkAccentBlue
                            )
                            activeStudy?.let {
                                Text(
                                    text = "IE Shopfloor Study Console",
                                    fontSize = 11.sp,
                                    color = ElegantDarkTextHelper
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(ElegantDarkDarkBlue)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "LIVE STUDY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElegantDarkAccentBlue
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ElegantDarkBg,
                    titleContentColor = ElegantDarkAccentBlue
                ),
                actions = {
                    // Export Actions using 100% Core Material Icons with theme color
                    IconButton(
                        onClick = { viewModel.exportPdfReport(context, share = false, print = false) },
                        modifier = Modifier.testTag("download_pdf_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Download Report", tint = ElegantDarkAccentBlue)
                    }
                    IconButton(
                        onClick = { viewModel.exportPdfReport(context, share = true, print = false) },
                        modifier = Modifier.testTag("share_pdf_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share Report", tint = ElegantDarkAccentBlue)
                    }
                    IconButton(
                        onClick = { showAuthorInfoDialog = true },
                        modifier = Modifier.testTag("author_info_button")
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "Author Profile", tint = ElegantDarkAccentBlue)
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab == 0 && stopwatchState.processId == 0) {
                FloatingActionButton(
                    onClick = {
                        val nextSl = (activeProcesses.maxOfOrNull { it.sl } ?: 0) + 1
                        showProcessEditDialog = StudyProcess(
                            id = 0,
                            studyId = activeStudy?.id ?: 0,
                            sl = nextSl,
                            processName = "",
                            mc = "SNL"
                        )
                    },
                    containerColor = ElegantDarkDarkBlue,
                    contentColor = ElegantDarkAccentBlue,
                    modifier = Modifier.testTag("add_process_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Process")
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(ElegantDarkBg)
        ) {
            // General administrative controls / Primary Workspace triggers inside top row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ElegantDarkBg)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showCreateStudyDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElegantDarkDarkBlue,
                            contentColor = ElegantDarkAccentBlue
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("new_entry_button").height(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Entry", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.createStudyFromDefaultTemplate() },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ElegantDarkAccentBlue
                        ),
                        border = BorderStroke(1.dp, ElegantDarkBorder),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("load_template_button").height(36.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Load Template", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Demo Data", fontSize = 12.sp)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = { showStudyEditDialog = true },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = ElegantDarkSurface,
                            contentColor = ElegantDarkAccentBlue
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("edit_study_button").height(36.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Study Details", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit Study", fontSize = 12.sp)
                    }

                    IconButton(
                        onClick = {
                            viewModel.resetAllObservationsInActiveStudy()
                            Toast.makeText(context, "All cycle timings reset!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("reset_all_button").size(36.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Observations", tint = ElegantDarkRedBg)
                    }

                    IconButton(
                        onClick = {
                            viewModel.deleteActiveStudy()
                            Toast.makeText(context, "Capacity Study deleted!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("delete_study_button").size(36.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Study", tint = ElegantDarkRedBg)
                    }
                }
            }

            // High Fidelity Header Metadata Horizontal Capsules (similar to HTML layout)
            activeStudy?.let { study ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(ElegantDarkSurface, RoundedCornerShape(12.dp))
                            .border(1.dp, ElegantDarkBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Column {
                            Text("LINE / OP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ElegantDarkTextHelper, letterSpacing = 0.5.sp)
                            Text(study.lineNo, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ElegantDarkTextMain)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .background(ElegantDarkSurface, RoundedCornerShape(12.dp))
                            .border(1.dp, ElegantDarkBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Column {
                            Text("BUYER", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ElegantDarkTextHelper, letterSpacing = 0.5.sp)
                            Text(study.buyer, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ElegantDarkTextMain, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .background(ElegantDarkSurface, RoundedCornerShape(12.dp))
                            .border(1.dp, ElegantDarkBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Column {
                            Text("STYLE NO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ElegantDarkTextHelper, letterSpacing = 0.5.sp)
                            Text(study.styleNo, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ElegantDarkTextMain, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Material 3 Navigation Tabs in Elegant Dark
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = ElegantDarkNavBg,
                contentColor = ElegantDarkAccentBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = ElegantDarkAccentBlue
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Timing Grid", fontWeight = FontWeight.SemiBold, color = if (selectedTab == 0) ElegantDarkAccentBlue else ElegantDarkTextHelper) },
                    icon = { Icon(Icons.Default.Home, contentDescription = null, tint = if (selectedTab == 0) ElegantDarkAccentBlue else ElegantDarkTextHelper) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Visual Capacity Graph", fontWeight = FontWeight.SemiBold, color = if (selectedTab == 1) ElegantDarkAccentBlue else ElegantDarkTextHelper) },
                    icon = { Icon(Icons.Default.Star, contentDescription = null, tint = if (selectedTab == 1) ElegantDarkAccentBlue else ElegantDarkTextHelper) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Saved Studies (${studies.size})", fontWeight = FontWeight.SemiBold, color = if (selectedTab == 2) ElegantDarkAccentBlue else ElegantDarkTextHelper) },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = if (selectedTab == 2) ElegantDarkAccentBlue else ElegantDarkTextHelper) }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Body content
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (selectedTab) {
                    0 -> StudyDashboard(
                        study = activeStudy,
                        processes = activeProcesses,
                        viewModel = viewModel,
                        stopwatchState = stopwatchState,
                        onEditProcess = { showProcessEditDialog = it },
                        onDeleteProcess = { viewModel.deleteProcess(it) }
                    )
                    1 -> VisualGraphView(
                        study = activeStudy,
                        processes = activeProcesses
                    )
                    2 -> SavedStudiesHistoryView(
                        studies = studies,
                        activeId = activeStudy?.id ?: -1,
                        onSelect = { viewModel.selectStudy(it) },
                        onDelete = { viewModel.deleteStudy(it) }
                    )
                }

                // Stopwatch Panel overlay (Fitted at top-right or overlaying bottom based on context)
                if (stopwatchState.processId > 0) {
                     val activeProc = activeProcesses.find { it.id == stopwatchState.processId }
                     activeProc?.let { p ->
                         StopwatchOverlay(
                             process = p,
                             state = stopwatchState,
                             onTapSplit = { viewModel.captureLapObservation() },
                             onCancel = { viewModel.stopAndCancelStopwatch() },
                             onPause = { viewModel.pauseStopwatch() },
                             onResume = { viewModel.resumeStopwatch() },
                             onReset = { viewModel.resetStopwatch() }
                         )
                     }
                }
            }
        }

        // --- ALL GENERAL DIALOGS ---

        // Edit Study Details
        if (showStudyEditDialog && activeStudy != null) {
            val s = activeStudy!!
            var editLineNo by remember { mutableStateOf(s.lineNo) }
            var editBuyer by remember { mutableStateOf(s.buyer) }
            var editStyleNo by remember { mutableStateOf(s.styleNo) }
            var editStudiedBy by remember { mutableStateOf(s.studiedBy) }
            var editDate by remember { mutableStateOf(s.date) }
            var editDaysRun by remember { mutableStateOf(s.daysRun) }
            var editGraphInfo by remember { mutableStateOf(s.graphInfo) }
            var editSmv by remember { mutableStateOf(s.smv) }
            var editTarget by remember { mutableStateOf(s.targetPerHour.toString()) }
            var editCurrentHr by remember { mutableStateOf(s.currentPcsPerHour.toString()) }

            AlertDialog(
                onDismissRequest = { showStudyEditDialog = false },
                title = { Text("Edit Cargo/Line Study Details") },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editLineNo,
                            onValueChange = { editLineNo = it },
                            label = { Text("Line No.") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_line_input")
                        )
                        OutlinedTextField(
                            value = editBuyer,
                            onValueChange = { editBuyer = it },
                            label = { Text("Buyer Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editStyleNo,
                            onValueChange = { editStyleNo = it },
                            label = { Text("Style No. / Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editStudiedBy,
                            onValueChange = { editStudiedBy = it },
                            label = { Text("Studied By (IE Engineer)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editDate,
                            onValueChange = { editDate = it },
                            label = { Text("Study Date") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editDaysRun,
                            onValueChange = { editDaysRun = it },
                            label = { Text("Days Run") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editGraphInfo,
                            onValueChange = { editGraphInfo = it },
                            label = { Text("Graph Info") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editSmv,
                            onValueChange = { editSmv = it },
                            label = { Text("SMV") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editTarget,
                            onValueChange = { editTarget = it },
                            label = { Text("Target Output per Hour") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("edit_target_input")
                        )
                        OutlinedTextField(
                            value = editCurrentHr,
                            onValueChange = { editCurrentHr = it },
                            label = { Text("Current Output per Hour") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val targetVal = editTarget.toIntOrNull() ?: 150
                            val currentVal = editCurrentHr.toIntOrNull() ?: 100
                            if (editLineNo.isBlank()) {
                                Toast.makeText(context, "Line No. is mandatory!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.updateActiveStudyDetails(
                                lineNo = editLineNo,
                                buyer = editBuyer,
                                styleNo = editStyleNo,
                                studiedBy = editStudiedBy,
                                date = editDate,
                                targetPerHour = targetVal,
                                currentPcsPerHour = currentVal,
                                daysRun = editDaysRun,
                                graphInfo = editGraphInfo,
                                smv = editSmv
                            )
                            showStudyEditDialog = false
                        }
                    ) {
                        Text("Save Details")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showStudyEditDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Author Info Dialog
        if (showAuthorInfoDialog) {
            AlertDialog(
                onDismissRequest = { showAuthorInfoDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = ElegantDarkAccentBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Author Profile",
                            fontWeight = FontWeight.Bold,
                            color = ElegantDarkTextMain,
                            fontSize = 18.sp
                        )
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Engr. M. A. Qaiyum Talukder",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = ElegantDarkAccentBlue
                        )
                        
                        Text(
                            text = "For any assistance, information, or professional inquiries, please feel free to contact:",
                            fontSize = 13.sp,
                            color = ElegantDarkTextHelper,
                            lineHeight = 18.sp
                        )
                        
                        HorizontalDivider(color = ElegantDarkBorder.copy(alpha = 0.5f))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+8801912895591"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Cannot open dialer", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Phone icon",
                                tint = ElegantDarkAccentGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Mobile: +880 1912-895591",
                                fontSize = 14.sp,
                                color = ElegantDarkTextMain,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:maqaiyumtalukder@gmail.com"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Cannot open email client", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email icon",
                                tint = ElegantDarkAccentBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Email: maqaiyumtalukder@gmail.com",
                                fontSize = 14.sp,
                                color = ElegantDarkTextMain,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAuthorInfoDialog = false }) {
                        Text(text = "CLOSE", color = ElegantDarkAccentBlue, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = ElegantDarkBg,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // New Study Dialog
        if (showCreateStudyDialog) {
            var newLineNo by remember { mutableStateOf("C") }
            var newBuyer by remember { mutableStateOf("") }
            var newStyleNo by remember { mutableStateOf("") }
            var newStudiedBy by remember { mutableStateOf("") }
            var newTarget by remember { mutableStateOf("150") }
            var newDaysRun by remember { mutableStateOf("") }
            var newGraphInfo by remember { mutableStateOf("") }
            var newSmv by remember { mutableStateOf("") }
            
            val sdf = SimpleDateFormat("d-MMM-yy", Locale.getDefault())
            val dateStr = sdf.format(Date())

            AlertDialog(
                onDismissRequest = { showCreateStudyDialog = false },
                title = { Text("Bootstrap New Capacity Study") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Bootstrap study fields. System automatically pre-allocates the standard 21 garments assembly sewing operations.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        OutlinedTextField(
                            value = newLineNo,
                            onValueChange = { newLineNo = it },
                            label = { Text("Line No. (Mandatory)") },
                            modifier = Modifier.fillMaxWidth().testTag("new_line_input")
                        )
                        OutlinedTextField(
                            value = newBuyer,
                            onValueChange = { newBuyer = it },
                            label = { Text("Buyer") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newStyleNo,
                            onValueChange = { newStyleNo = it },
                            label = { Text("Style No / Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newStudiedBy,
                            onValueChange = { newStudiedBy = it },
                            label = { Text("Studied By") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newDaysRun,
                            onValueChange = { newDaysRun = it },
                            label = { Text("Days Run") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newGraphInfo,
                            onValueChange = { newGraphInfo = it },
                            label = { Text("Graph Info") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newSmv,
                            onValueChange = { newSmv = it },
                            label = { Text("SMV") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newTarget,
                            onValueChange = { newTarget = it },
                            label = { Text("Hourly Target (Pcs)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("new_target_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newLineNo.isBlank()) {
                                Toast.makeText(context, "Line number cannot be blank!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.createNewBlankStudy(
                                lineNo = newLineNo,
                                buyer = newBuyer.ifBlank { "N/A" },
                                styleNo = newStyleNo.ifBlank { "N/A" },
                                studiedBy = newStudiedBy.ifBlank { "N/A" },
                                date = dateStr,
                                targetPerHour = newTarget.toIntOrNull() ?: 150,
                                daysRun = newDaysRun,
                                graphInfo = newGraphInfo,
                                smv = newSmv
                            )
                            showCreateStudyDialog = false
                            selectedTab = 0
                        }
                    ) {
                        Text("Create Workspace")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateStudyDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Add / Edit Process Custom Row dialog
        showProcessEditDialog?.let { process ->
            var slStr by remember { mutableStateOf(process.sl.toString()) }
            var processName by remember { mutableStateOf(process.processName) }
            var mcVal by remember { mutableStateOf(process.mc) }
            var relation by remember { mutableStateOf(process.relation) }
            
            var op1Name by remember { mutableStateOf(process.op1Name) }
            var op2Name by remember { mutableStateOf(process.op2Name) }
            var op3Name by remember { mutableStateOf(process.op3Name) }

            AlertDialog(
                onDismissRequest = { showProcessEditDialog = null },
                title = { Text(if (process.id == 0) "Add Operation / Process Flow" else "Edit Operation Flow Detail") },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = slStr,
                            onValueChange = { slStr = it },
                            label = { Text("Sequence No (SL)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("sl_input")
                        )
                        OutlinedTextField(
                            value = processName,
                            onValueChange = { processName = it },
                            label = { Text("Process/Operation Name (Mandatory)") },
                            modifier = Modifier.fillMaxWidth().testTag("process_name_input")
                        )
                        OutlinedTextField(
                            value = mcVal,
                            onValueChange = { mcVal = it },
                            label = { Text("M/C Type (SNL, OL, BH... Write 'M' for helper, 'IM' for ironman)") },
                            modifier = Modifier.fillMaxWidth().testTag("mc_input")
                        )

                        Text("Relation Relationship (Multiple Operators)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = relation == "Parallel", onClick = { relation = "Parallel" })
                                Text("Parallel")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = relation == "Sequence", onClick = { relation = "Sequence" })
                                Text("Sequence Split")
                            }
                        }

                        OutlinedTextField(
                            value = op1Name,
                            onValueChange = { op1Name = it },
                            label = { Text("Op-1 Operator Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = op2Name,
                            onValueChange = { op2Name = it },
                            label = { Text("Op-2 Operator Name (Optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = op3Name,
                            onValueChange = { op3Name = it },
                            label = { Text("Op-3 Operator name (optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (processName.isBlank()) {
                                Toast.makeText(context, "Process Name is mandatory!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val sNum = slStr.toIntOrNull() ?: process.sl
                            val updatedProcess = process.copy(
                                sl = sNum,
                                processName = processName,
                                mc = mcVal.uppercase(),
                                relation = relation,
                                op1Name = op1Name,
                                op2Name = op2Name,
                                op3Name = op3Name
                            )
                            viewModel.saveProcessDetail(updatedProcess)
                            showProcessEditDialog = null
                        }
                    ) {
                        Text("Save Action")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showProcessEditDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

// Subcomponents

@Composable
fun StudyDashboard(
    study: CapacityStudy?,
    processes: List<StudyProcess>,
    viewModel: CapacityStudyViewModel,
    stopwatchState: StopwatchState,
    onEditProcess: (StudyProcess) -> Unit,
    onDeleteProcess: (StudyProcess) -> Unit
) {
    if (study == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Core Excel Metric Header
        item {
            ExcelMetricsHeader(study = study, processes = processes)
        }

        // Table List rows
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkCardGrid),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ElegantDarkBorder)
            ) {
                // Table header labels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ElegantDarkSurface)
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("SL", fontWeight = FontWeight.Bold, color = ElegantDarkTextHelper, fontSize = 11.sp, modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
                    Text("PROCESS NAME", fontWeight = FontWeight.Bold, color = ElegantDarkTextHelper, fontSize = 11.sp, modifier = Modifier.weight(1.8f))
                    Text("M/C", fontWeight = FontWeight.Bold, color = ElegantDarkTextHelper, fontSize = 11.sp, modifier = Modifier.weight(0.8f))
                    Text("OPERATORS & OBSERVATIONS", fontWeight = FontWeight.Bold, color = ElegantDarkTextHelper, fontSize = 11.sp, modifier = Modifier.weight(4.0f))
                    Text("CAP", fontWeight = FontWeight.Bold, color = ElegantDarkTextHelper, fontSize = 11.sp, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
                    Spacer(modifier = Modifier.width(70.dp)) // Space for Edit/Delete
                }
            }
        }

        if (processes.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    Text("No operations set. Press edit study or bootstrap new study.", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            items(processes, key = { it.id }) { process ->
                val isTimingThisRow = stopwatchState.processId == process.id
                RowGridItem(
                    process = process,
                    isTiming = isTimingThisRow,
                    activeOpIndex = stopwatchState.operatorIndex,
                    targetVal = study.targetPerHour,
                    onEdit = { onEditProcess(process) },
                    onDelete = { onDeleteProcess(process) },
                    onStartTimer = { opIndex -> viewModel.startStopwatch(process.id, opIndex) }
                )
            }
        }
    }
}

@Composable
fun ExcelMetricsHeader(
    study: CapacityStudy,
    processes: List<StudyProcess>
) {
    // Computations
    val totalManpower = processes.sumOf {
        it.getOperatorCountByMachineCategory("M") +
        it.getOperatorCountByMachineCategory("IM") +
        it.getOperatorCountByMachineCategory("OPERATOR")
    }
    val totalOperators = processes.sumOf { it.getOperatorCountByMachineCategory("OPERATOR") }
    val totalHelpers = processes.sumOf { it.getOperatorCountByMachineCategory("M") }
    val totalIronman = processes.sumOf { it.getOperatorCountByMachineCategory("IM") }
    
    val totalCycles = processes.sumOf { 
        (if (it.isOp1Active()) it.getOp1Average() else 0.0) +
        (if (it.isOp2Active()) it.getOp2Average() else 0.0) +
        (if (it.isOp3Active()) it.getOp3Average() else 0.0)
    }
    val basicPitchTime = if (totalManpower > 0) totalCycles / totalManpower else 0.0
    val workerPotential = if (basicPitchTime > 0) 3600.0 / basicPitchTime else 0.0
    val productivityGap = if (workerPotential > 0) ((study.currentPcsPerHour - workerPotential) / workerPotential) * 100 else 0.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkCardActive),
        border = BorderStroke(1.dp, ElegantDarkBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // General Info line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PRODUCTION MONITOR DATA BLOCK",
                    color = ElegantDarkAccentBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Date: ${study.date}",
                    color = ElegantDarkTextHelper,
                    fontSize = 11.sp
                )
            }
            HorizontalDivider(color = ElegantDarkBorder, modifier = Modifier.padding(vertical = 8.dp))

            // Grid metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Manpower block
                Column(modifier = Modifier.weight(1f)) {
                    MetricMiniRow(label = "TOTAL MANPOWER", value = totalManpower.toString(), isHighlight = true)
                    MetricMiniRow(label = "TOTAL OPERATOR", value = totalOperators.toString())
                    MetricMiniRow(label = "TOTAL HELPER", value = totalHelpers.toString())
                    MetricMiniRow(label = "TOTAL IRONMAN", value = totalIronman.toString())
                }

                // Balance block
                Column(modifier = Modifier.weight(1f)) {
                    MetricMiniRow(label = "TOTAL CYCLE TIME", value = String.format("%.2fs", totalCycles))
                    MetricMiniRow(label = "BASIC PITCH TIME", value = String.format("%.2fs", basicPitchTime))
                    MetricMiniRow(label = "WORKER POTENTIAL / HR", value = String.format("%.0f pcs", workerPotential), isHighlight = true)
                    
                    val colorGap = if (productivityGap >= 0) ElegantDarkAccentGreen else ElegantDarkRedBg
                    MetricMiniRow(
                        label = "PRODUCTIVITY GAP %",
                        value = String.format("%.2f%%", productivityGap),
                        valueColor = colorGap
                    )
                }

                // Target constraints
                Column(modifier = Modifier.weight(0.9f)) {
                    MetricMiniRow(label = "LINE TARGET / HR", value = "${study.targetPerHour} pcs", isHighlight = true)
                    MetricMiniRow(label = "CURRENT PCS / HR", value = "${study.currentPcsPerHour} pcs")
                    MetricMiniRow(label = "STUDIED BY", value = study.studiedBy)
                    MetricMiniRow(label = "DAYS RUN", value = study.daysRun.ifBlank { "N/A" })
                    MetricMiniRow(label = "GRAPH INFO", value = study.graphInfo.ifBlank { "N/A" })
                    MetricMiniRow(label = "SMV", value = study.smv.ifBlank { "N/A" })
                }
            }
        }
    }
}

@Composable
fun MetricMiniRow(
    label: String,
    value: String,
    isHighlight: Boolean = false,
    valueColor: Color = ElegantDarkTextMain
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            color = if (isHighlight) ElegantDarkTextMain else ElegantDarkTextHelper,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1.3f)
        )
        Text(
            text = value,
            fontSize = 10.sp,
            color = if (isHighlight) ElegantDarkAccentBlue else valueColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.8f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun RowGridItem(
    process: StudyProcess,
    isTiming: Boolean,
    activeOpIndex: Int,
    targetVal: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStartTimer: (Int) -> Unit
) {
    val capacities = process.getOpCapacities()
    val totalCapacity = capacities.first + capacities.second + capacities.third

    val alertBg = if (totalCapacity <= 0) {
        ElegantDarkCardGrid
    } else if (totalCapacity >= targetVal) {
        ElegantDarkAccentGreen.copy(alpha = 0.15f)
    } else if (totalCapacity >= targetVal * 0.8) {
        Amber500.copy(alpha = 0.15f)
    } else {
        ElegantDarkRedBg.copy(alpha = 0.12f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = alertBg),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, ElegantDarkBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sequence Number
            Text(
                text = process.sl.toString(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(30.dp),
                textAlign = TextAlign.Center,
                color = ElegantDarkTextHelper
            )

            // Operation Name
            Column(modifier = Modifier.weight(1.8f).padding(end = 4.dp)) {
                Text(
                    text = process.processName.uppercase(),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElegantDarkTextMain,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (process.relation != "Parallel") {
                    Text(
                        text = "Sequence logic split",
                        fontSize = 8.sp,
                        color = ElegantDarkTextHelper,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // M/C column
            Text(
                text = process.mc,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = ElegantDarkAccentBlue,
                modifier = Modifier.weight(0.8f)
            )

            // Operator Columns + Split observations
            Column(modifier = Modifier.weight(4.0f)) {
                // OP 1 Block
                if (process.isOp1Active()) {
                    OperatorTimingsRow(
                        opName = process.op1Name,
                        cycles = listOf(process.op1C1, process.op1C2, process.op1C3, process.op1C4, process.op1C5),
                        isTimingThisOperator = isTiming && activeOpIndex == 1,
                        avg = process.getOp1Average(),
                        cap = capacities.first,
                        onTriggerWatch = { onStartTimer(1) }
                    )
                } else {
                    OperatorUnconfiguredRow(opLabel = "OP-1", onTrigger = { onStartTimer(1) })
                }

                // OP 2 Block
                if (process.isOp2Active()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    OperatorTimingsRow(
                        opName = process.op2Name,
                        cycles = listOf(process.op2C1, process.op2C2, process.op2C3, process.op2C4, process.op2C5),
                        isTimingThisOperator = isTiming && activeOpIndex == 2,
                        avg = process.getOp2Average(),
                        cap = capacities.second,
                        onTriggerWatch = { onStartTimer(2) }
                    )
                } else if (process.op2Name.isNotBlank() || process.relation == "Sequence") {
                    Spacer(modifier = Modifier.height(2.dp))
                    OperatorUnconfiguredRow(opLabel = "OP-2", onTrigger = { onStartTimer(2) })
                }

                // OP 3 Block
                if (process.isOp3Active()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    OperatorTimingsRow(
                        opName = process.op3Name,
                        cycles = listOf(process.op3C1, process.op3C2, process.op3C3, process.op3C4, process.op3C5),
                        isTimingThisOperator = isTiming && activeOpIndex == 3,
                        avg = process.getOp3Average(),
                        cap = capacities.third,
                        onTriggerWatch = { onStartTimer(3) }
                    )
                } else if (process.op3Name.isNotBlank() || process.relation == "Sequence") {
                    Spacer(modifier = Modifier.height(2.dp))
                    OperatorUnconfiguredRow(opLabel = "OP-3", onTrigger = { onStartTimer(3) })
                }
            }

            // Cap block total
            Text(
                text = String.format("%.0f/h", totalCapacity),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (totalCapacity >= targetVal) ElegantDarkAccentGreen else if (totalCapacity >= targetVal * 0.8) Amber500 else ElegantDarkRedText,
                modifier = Modifier.weight(0.8f),
                textAlign = TextAlign.End
            )

            // Edit / delete action
            Row(
                modifier = Modifier.width(70.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp).testTag("edit_process_${process.sl}")) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Block", modifier = Modifier.size(16.dp), tint = ElegantDarkTextHelper)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp).testTag("delete_process_${process.sl}")) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Block", modifier = Modifier.size(16.dp), tint = ElegantDarkRedBg)
                }
            }
        }
    }
}

@Composable
fun OperatorTimingsRow(
    opName: String,
    cycles: List<Double>,
    isTimingThisOperator: Boolean,
    avg: Double,
    cap: Double,
    onTriggerWatch: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(ElegantDarkSurface.copy(alpha = 0.5f))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onTriggerWatch,
            modifier = Modifier.size(24.dp).background(if (isTimingThisOperator) ElegantDarkRedBg else ElegantDarkDarkBlue, RoundedCornerShape(4.dp))
        ) {
            Icon(
                imageVector = if (isTimingThisOperator) Icons.Default.Close else Icons.Default.PlayArrow,
                contentDescription = "Stopwatch trigger",
                tint = if (isTimingThisOperator) ElegantDarkRedText else ElegantDarkAccentBlue,
                modifier = Modifier.size(13.dp)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = opName.ifBlank { "Unknown" }.uppercase(),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElegantDarkTextMain,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "avg: ${String.format("%.1fs", avg)}",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ElegantDarkTextActive
                )
            }
            
            // Cycle splits inline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                cycles.forEachIndexed { i, c ->
                    val displayC = if (c > 0.0) String.format("%.1f", c) else "-"
                    Text(
                        text = displayC,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (c > 0.0) ElegantDarkTextMain else ElegantDarkTextHelper.copy(alpha = 0.5f),
                        modifier = Modifier
                            .weight(1f)
                            .background(ElegantDarkCardGrid, RoundedCornerShape(4.dp))
                            .padding(vertical = 1.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun OperatorUnconfiguredRow(
    opLabel: String,
    onTrigger: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable { onTrigger() }
            .padding(vertical = 4.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = ElegantDarkTextHelper)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Click to assign and time $opLabel",
            fontSize = 9.5.sp,
            color = ElegantDarkTextHelper,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun StopwatchOverlay(
    process: StudyProcess,
    state: StopwatchState,
    onTapSplit: () -> Unit,
    onCancel: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkCardActive),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, ElegantDarkAccentBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ACTIVE SHOPFLOOR CYCLE MEASUREMENT",
                        color = ElegantDarkAccentBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Process: ${process.processName} | Op: ${
                            when (state.operatorIndex) {
                                1 -> process.op1Name.uppercase()
                                2 -> process.op2Name.uppercase()
                                3 -> process.op3Name.uppercase()
                                else -> "UNKNOWN"
                            }
                        }",
                        color = ElegantDarkTextMain,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .size(28.dp)
                        .background(ElegantDarkSurface, CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close stopwatch", tint = ElegantDarkAccentBlue, modifier = Modifier.size(14.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Stopwatch digital display
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                val totalSec = state.elapsedMs / 1000f
                Text(
                    text = String.format("%.2f", totalSec),
                    fontSize = 34.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = if (state.isPaused) ElegantDarkAccentBlue.copy(alpha = 0.7f) else ElegantDarkTextMain
                )
                Text(
                    text = "s",
                    fontSize = 16.sp,
                    color = ElegantDarkTextHelper,
                    modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Lap counters
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 5) {
                    val isActive = i < state.laps.size
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isActive) ElegantDarkAccentGreen else ElegantDarkTextHelper.copy(alpha = 0.2f))
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${state.laps.size} of 5 Observations Done",
                    color = ElegantDarkTextMain,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Recorded Laps details
            if (state.laps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().background(ElegantDarkSurface, RoundedCornerShape(8.dp)).padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    state.laps.forEachIndexed { idx, lap ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Lap ${idx+1}", color = ElegantDarkTextHelper, fontSize = 8.sp)
                            Text(String.format("%.1fs", lap), color = ElegantDarkTextMain, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Giant active touch button
            Button(
                onClick = onTapSplit,
                enabled = state.isRunning || state.isPaused,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.laps.size >= 4) ElegantDarkAccentGreen else ElegantDarkDarkBlue,
                    contentColor = if (state.laps.size >= 4) ElegantDarkTextDarkBlue else ElegantDarkAccentBlue,
                    disabledContainerColor = ElegantDarkSurface.copy(alpha = 0.3f),
                    disabledContentColor = ElegantDarkTextHelper.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("lap_capture_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (state.laps.size >= 4) ElegantDarkTextDarkBlue else ElegantDarkAccentBlue
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (state.laps.size >= 4) "FINISH MEASUREMENT" else "TAP ON PIECE COMPLETION",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Action CTAs: Pushed (Pause), Reset, Resume Row
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Pause button (labeled PAUSE / PUSHED)
                Button(
                    onClick = onPause,
                    enabled = state.isRunning,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElegantDarkSurface,
                        contentColor = ElegantDarkAccentBlue,
                        disabledContainerColor = ElegantDarkSurface.copy(alpha = 0.2f),
                        disabledContentColor = ElegantDarkTextHelper.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("btn_stopwatch_pushed")
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Pause", modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("PAUSE", fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }

                // Resume button
                Button(
                    onClick = onResume,
                    enabled = state.isPaused,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElegantDarkDarkBlue,
                        contentColor = ElegantDarkAccentBlue,
                        disabledContainerColor = ElegantDarkSurface.copy(alpha = 0.2f),
                        disabledContentColor = ElegantDarkTextHelper.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("btn_stopwatch_resume")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Resume", modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("RESUME", fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }

                // Reset button
                Button(
                    onClick = onReset,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElegantDarkRedBg.copy(alpha = 0.2f),
                        contentColor = ElegantDarkRedText
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("btn_stopwatch_reset")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("RESET", fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun VisualGraphView(
    study: CapacityStudy?,
    processes: List<StudyProcess>
) {
    if (study == null) return

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ElegantDarkCardGrid),
            border = BorderStroke(1.dp, ElegantDarkBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CAPACITY GRAPH (BARS PLOTTED COMPARED TO GOAL)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElegantDarkTextMain
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ElegantDarkRedBg.copy(alpha = 0.15f))
                    ) {
                        Text(
                            text = "Target: ${study.targetPerHour} pcs/hr",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElegantDarkRedText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom bar chart
                val maxRange = (processes.maxOfOrNull {
                    val caps = it.getOpCapacities()
                    caps.first + caps.second + caps.third
                } ?: 300.0).coerceAtLeast(study.targetPerHour.toDouble() * 1.5).coerceAtLeast(100.0)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    processes.forEach { process ->
                        val caps = process.getOpCapacities()
                        val cap = caps.first + caps.second + caps.third
                        
                        val ratio = (cap / maxRange).toFloat().coerceIn(0f, 1f)
                        
                        val barColor = if (cap <= 0.0) {
                            ElegantDarkBorder
                        } else if (cap >= study.targetPerHour) {
                            ElegantDarkAccentGreen
                        } else if (cap >= study.targetPerHour * 0.8) {
                            Amber500
                        } else {
                            ElegantDarkRedBg
                        }

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${process.sl}. ${process.processName}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElegantDarkTextMain,
                                    modifier = Modifier.weight(1.5f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = String.format("%.0f pcs/hr", cap),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = barColor,
                                    modifier = Modifier.weight(0.7f),
                                    textAlign = TextAlign.End
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(14.dp)
                                    .background(ElegantDarkSurface, RoundedCornerShape(3.dp))
                            ) {
                                // Red line target overlay
                                val targetRatio = (study.targetPerHour / maxRange).toFloat()
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val targetOffset = size.width * targetRatio
                                    drawLine(
                                        color = ElegantDarkRedBg,
                                        start = androidx.compose.ui.geometry.Offset(targetOffset, 0f),
                                        end = androidx.compose.ui.geometry.Offset(targetOffset, size.height),
                                        strokeWidth = 2f
                                    )
                                }

                                if (ratio > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(ratio)
                                            .background(barColor, RoundedCornerShape(3.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Legends
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElegantDarkCardGrid)
                .border(1.dp, ElegantDarkBorder, RoundedCornerShape(12.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            LegendItem(color = ElegantDarkAccentGreen, label = "Target Met (>= Target)")
            LegendItem(color = Amber500, label = "Caution (>= 80% Target)")
            LegendItem(color = ElegantDarkRedBg, label = "Bottleneck (< 80% Target)")
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = ElegantDarkTextHelper)
    }
}

@Composable
fun SavedStudiesHistoryView(
    studies: List<CapacityStudy>,
    activeId: Int,
    onSelect: (Int) -> Unit,
    onDelete: (CapacityStudy) -> Unit
) {
    if (studies.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No saved studies available.", color = ElegantDarkTextHelper)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "SAVED CAPACITY REPOSITORY RECORDS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ElegantDarkAccentBlue,
                letterSpacing = 1.sp
            )
        }

        items(studies, key = { it.id }) { item ->
            val isActive = item.id == activeId
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(item.id) }
                    .testTag("study_record_${item.id}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive) ElegantDarkCardActive else ElegantDarkCardGrid
                ),
                border = BorderStroke(
                    1.dp,
                    if (isActive) ElegantDarkAccentBlue else ElegantDarkBorder
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Buyer: ${item.buyer}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = ElegantDarkTextMain
                        )
                        Text(
                            text = "Style: ${item.styleNo} | Line No: ${item.lineNo}",
                            fontSize = 11.5.sp,
                            color = ElegantDarkTextHelper
                        )
                        Text(
                            text = "Studied By: ${item.studiedBy} | Date: ${item.date}",
                            fontSize = 10.sp,
                            color = ElegantDarkTextHelper.copy(alpha = 0.7f)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isActive) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = ElegantDarkDarkBlue),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    "ACTIVE WORKSPACE",
                                    color = ElegantDarkAccentBlue,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Elegant, high-contrast, easy-to-tap M3 Delete button
                        IconButton(
                            onClick = { onDelete(item) },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("delete_saved_study_${item.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Study",
                                tint = ElegantDarkRedText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

val Icons_Default_Print: ImageVector
    get() {
        return ImageVector.Builder(
            name = "Filled.Print",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(19.0f, 8.0f)
                horizontalLineTo(5.0f)
                curveToRelative(-1.66f, 0.0f, -3.0f, 1.34f, -3.0f, 3.0f)
                verticalLineToRelative(6.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(4.0f)
                horizontalLineToRelative(12.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(-6.0f)
                curveToRelative(0.0f, -1.66f, -1.34f, -3.0f, -3.0f, -3.0f)
                close()
                moveTo(16.0f, 19.0f)
                horizontalLineTo(8.0f)
                verticalLineToRelative(-5.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(5.0f)
                close()
                moveTo(19.0f, 12.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                close()
                moveTo(18.0f, 3.0f)
                horizontalLineTo(6.0f)
                verticalLineToRelative(4.0f)
                horizontalLineToRelative(12.0f)
                verticalLineTo(3.0f)
                close()
            }
        }.build()
    }

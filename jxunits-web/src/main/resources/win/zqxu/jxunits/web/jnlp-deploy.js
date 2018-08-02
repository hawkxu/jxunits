var deployUtils = (function($) {
  var projectGrid, projectForm, libraryGrid, uploadLibsGrid;
  var obj = {
    phrases: {},
    lang: lang,
    locale: locale,
    initHomePage: initHomePage,
    goDeployPage: goDeployPage,
    initDeployPage: initDeployPage
  };
  return obj;

  function lang(phrase) {
    var language = this.phrases[phrase];
    return language ? language : phrase;
  }

  function locale(locale, complete) {
    if (typeof locale === 'function' && !complete) {
      complete = locale;
      locale = undefined;
    }
    if (!locale) locale = navigator.language;
    locale = "locale_" + locale + ".json";
    $.ajax({
      url: locale,
      type: "GET",
      dataType: "JSON",
      success: function(data, status, xhr) {
        deployUtils.phrases = data;
      },
      error: function(xhr, status, msg) {
        console.log('ERROR: Cannot load locale ' + locale);
      },
      complete: function(xhr, status) {
        if (typeof complete == 'function') complete();
      }
    });
  }

  function initHomePage() {
    projectGrid = $("#projectGrid").w2grid({
      name: "projectGrid",
      header: deployUtils.lang("Project List"),
      style: "width: 700px; height: 500px; margin: 0 auto",
      show: {
        header: true
      },
      url: "_list",
      method: "GET",
      recid: "name",
      columns: [
        {field: "name", caption: deployUtils.lang("Name"), size: "30%", render: renderProjectName},
        {field: "title", caption: deployUtils.lang("Title"), size: "50%", render: renderProjectTitle},
        {field: "version", caption: deployUtils.lang("Version"), size: "10%", render: renderProjectVersion},
        {caption: deployUtils.lang("Deploy"), size: "10%", render: renderProjectDeploy}
      ]
    });
  }
  
  function goDeployPage(projectName) {
    if (projectName === "$add") {
      var label = deployUtils.lang("Project Name");
      var title = deployUtils.lang("New Project");
      w2prompt(label, title).ok(function(value) {
        if (value.match(/^\w+/))
          goDeployPage(value);
        else
          w2alert(deployUtils.lang("Invalid Project Name"));
      });
    } else {
      window.location = "deploy/" + projectName + "/";
    }
  }
  
  function initDeployPage() {
    projectForm = $("#projectForm").w2form({
      name: "projectForm",
      header: deployUtils.lang("Deploy Project"),
      style: "width: 700px; height: 600px; margin: 0 auto",
      toolbar: {
        tooltip: null,   // set toolbar tooltip to null to avoid w2tag on button removed while button hover
        items: [
          {id: "back", disabled: false, text: deployUtils.lang("Back"), icon: "icon-deploy-back jnlp-tool-button"},
          {id: "change", disabled: false, text: deployUtils.lang("Change"), icon: "icon-deploy-change jnlp-tool-button"},
          {id: "jnlp", disabled: true, text: deployUtils.lang("Upload JNLP"), icon: "icon-deploy-jnlp jnlp-tool-button"},
          {id: "disable", disabled: true, text: deployUtils.lang("Disable"), icon: "icon-deploy-disable jnlp-tool-button"},
          {id: "enable", disabled: true, hidden: true, text: deployUtils.lang("Enable"), icon: "icon-deploy-enable jnlp-tool-button"},
          {id: "delete", disabled: true, text: deployUtils.lang("Delete"), icon: "icon-deploy-delete jnlp-tool-button"},
          {id: "restore", disabled: true, hidden: true, text: deployUtils.lang("Restore"), icon: "icon-deploy-restore jnlp-tool-button"},
          {type: "spacer"},
          {id: "save", disabled: true, text: deployUtils.lang("Save"), icon: "icon-deploy-save jnlp-tool-button"},
          {id: "cancel", disabled: true, text: deployUtils.lang("Cancel"), icon: "icon-deploy-cancel jnlp-tool-button"}
        ],
        onClick: handleProjectAction
       },
      fields: [
        {field: "name", type: "text"},
        {field: "version", type: "text"},
        {field: "title", type: "text"},
        {field: "moduleBaseClass", type: "text"},
        {field: "moduleNameGetter", type: "text"},
        {field: "moduleVersionGetter", type: "text"}
      ],
      onChange: handleProjectChange,
      onRefresh: handleProjectRefresh
    });
    libraryGrid = $("#libraryGrid").w2grid({
      name: "libraryGrid",
      header: deployUtils.lang("Libraries"),
      show: {
        header: true,
        toolbar: true,
        toolbarReload: false,
        toolbarColumns: false,
        toolbarInput: false
      },
      toolbar: {
        items: [
          {id: "add", type: "button", disabled: true, text: deployUtils.lang("Add"), icon: "icon-deploy-add jnlp-tool-button"},
          {id: "disable", type: "button", disabled: true, text: deployUtils.lang("Disable"), icon: "icon-deploy-disable jnlp-tool-button"},
          {id: "enable", type: "button", disabled: true, hidden: true, text: deployUtils.lang("Enable"), icon: "icon-deploy-enable jnlp-tool-button"},
          {id: "delete", type: "button", disabled: true, text: deployUtils.lang("Delete"), icon: "icon-deploy-delete jnlp-tool-button"},
          {id: "restore", type: "button", disabled: true, hidden: true, text: deployUtils.lang("Restore"), icon: "icon-deploy-restore jnlp-tool-button"},
          {type: "breaker"},
          {id: "up", type: "button", disabled: true, text: deployUtils.lang("Move Up"), icon: "icon-deploy-up jnlp-tool-button"},
          {id: "down", type: "button", disabled: true, text: deployUtils.lang("Move Down"), icon: "icon-deploy-down jnlp-tool-button"}
        ],
        onClick: handleLibraryAction
      },
      recid: "name",
      columns: [
        {field: "name", caption: deployUtils.lang("Library Name/Module"), size: "50%"},
        {field: "version", caption: deployUtils.lang("Version"), size: "15%"},
        {field: "fileSize", caption: deployUtils.lang("File Size"), size: "15%", render: renderLibrarySize},
        {field: "major", caption: deployUtils.lang("Main"), size: "40px", render: renderLibraryMajor},
        {field: "disabled", caption: deployUtils.lang("Disabled"), size: "40px", render: renderLibraryDisabled},
        {field: "deleted", caption: deployUtils.lang("Deleted"), size: "40px", render: renderLibraryDeleted}
      ],
      multiSelect: false,
      onSelect: handleLibrarySelect
    });
    libraryGrid.show.expandColumn = false;
    $("#icon_default").attr("title", deployUtils.lang("Default Icon"));
    $("#icon_shortcut").attr("title", deployUtils.lang("Shortcut Icon"));
    $("#icon_splash").attr("title", deployUtils.lang("Splash Image"));
    reloadProject(true);
    window.onbeforeunload = cleanDeployPage;
  }

  /** *----------------------------------- internal use -----------------------------------** */
  function mergeW2uiGridChanges(record) {
    return $.extend({}, record, record && record.w2ui && record.w2ui.changes);
  }
  
  function isLibraryRecord(library) {
    return !library.w2ui || !library.w2ui.parent_recid;
  }
  
  function renderProjectName(project, index, column) {
    if (project["name"] === "$add") return;
    return "<a href='" + project["name"] + "/'>" + project["name"] + "</a>";
  }
  
  function renderProjectTitle(project, index, column) {
    if (project["name"] == "$add") {
      return "<a href='#' onclick='" + getDeployProjectOnClick("$add") + "'>"
            + deployUtils.lang("Deploy New Project") + "</a>";
    }
    return "<a href='" + project["name"] + "/'>" + project["title"] + "</a>";
  }
  
  function renderProjectVersion(project, index, column) {
    if (project["name"] === "$add") return;
    return "<a href='" + project["name"] + "/'>" + project["version"] + "</a>";
  }
  
  function renderProjectDeploy(project, index, column) {
    if (!project["deployAllowed"]) return;
    var icon_class = "icon-project-" + (project["name"] === "$add" ? "add" : "edit");
    var deploy_title = project["name"] === "$add" ? "New Project" : "Update Project";
    return "<a href='#' title='" + deployUtils.lang(deploy_title)
          + "' onclick='" + getDeployProjectOnClick(project["name"]) + "'>"
          + "<span class='jnlp-w2cell-icon " + icon_class + "'></span></a>";
  }
  
  function getDeployProjectOnClick(projectName) {
    return "deployUtils.goDeployPage(&quot;" + projectName + "&quot;); return false;";
  }
  
  function renderLibrarySize(library, index, column) {
    library = mergeW2uiGridChanges(library);
    return isLibraryRecord(library) ? formatBytes(library.fileSize) : null;
  }
  
  function renderLibraryMajor(library, index, column) {
    return renderLibraryCheckBoxCell(library, "major");
  }

  function renderLibraryDisabled(library, index, column) {
    return renderLibraryCheckBoxCell(library, "disabled");
  }
  
  function renderLibraryDeleted(library, index, column) {
    return renderLibraryCheckBoxCell(library, "deleted");
  }
  
  function renderLibraryCheckBoxCell(library, field) {
    library = mergeW2uiGridChanges(library);
    if (isLibraryRecord(library)) // only render library row
      return "<div style='text-align: center'><input type='checkbox' disabled"
           + (library[field] ? " checked" : "") + "/></div>";
  }
  
  function handleProjectChange(event) {
    event.onComplete = function(event) {
      updateProject(undefined);
    }
  }
  
  function handleProjectRefresh(event) {
    event.onComplete = function(event) {
      setFieldLabel(this.get("name"), "Name");
      setFieldLabel(this.get("version"), "Version");
      setFieldLabel(this.get("title"), "Title");
      var classField = this.get("moduleBaseClass");
      var nameField = this.get("moduleNameGetter");
      var versionField = this.get("moduleVersionGetter");
      setFieldLabel(classField, "Module Base Class");
      setFieldLabel(nameField, "Module Name Getter");
      setFieldLabel(versionField, "Module Version Getter");
      setFieldPlaceholder(classField, "Base class or interface for module (Optional)");
      setFieldPlaceholder(nameField, "Getter method for module name (Optional)");
      setFieldPlaceholder(versionField, "Getter method for module version (Optional)");
      $("#icons_label").text(deployUtils.lang("Icons:"));
    }
  }
  
  function setFieldLabel(field, label) {
    var box = field.$el.parent().parent();
    label = deployUtils.lang(label);
    box.find(">label").text(label + ":");
  }
  
  function setFieldPlaceholder(field, placeholder) {
    field.$el.attr("placeholder", deployUtils.lang(placeholder));
  }
  
  function reloadProject(initial) {
    callServerAjax({
      locker: projectForm,
      lockMsg: "Loading project...",
      url: "_project"
    }).success(function(data, status, xhr) {
      data = data || {};
      projectForm.record = data;
      projectForm.origin = $.extend({}, data);
      projectForm.refresh();
      reloadIcons();
      reloadLibraryGrid();
      updateDeployState(false);
      if (data.initial && initial)
        projectForm.toolbar.click("change");
    });
  }
  
  function reloadIcons() {
    callServerAjax({
      locker: projectForm,
      lockMsg: "Loading icons...",
      url: "_icons"
    }).success(function(data, status, xhr) {
      data = data || {};
      if ("default" in data)
        $("#icon_default").css("background-image", "url('" + data["default"] + "')");
      if ("shortcut" in data)
        $("#icon_shortcut").css("background-image", "url('" + data["shortcut"] + "')");
      if ("splash" in data)
        $("#icon_splash").css("background-image", "url('" + data["splash"] + "')");
    });
  }
  
  function reloadLibraryGrid() {
    callServerAjax({
      locker: libraryGrid,
      lockMsg: "Loading libraries...",
      url: "_libs"
    }).success(function(data, status, xhr){
      data = data || [];
      for (var i in data)
        resetLibraryChildren(data[i]);
      libraryGrid.records = data;
      libraryGrid.refresh();
    })
  }
  
  function resetLibraryChildren(library) {
    var w2ui = library.w2ui || {};
    w2ui.children = [];
    var merged = mergeW2uiGridChanges(library);
    var modules = merged.modules || [];
    for (var index in modules) {
      var module = modules[index];
      module.recid = module.className;
      w2ui.children.push(module);
    }
    library.w2ui = w2ui;
  }
  
  function handleLibrarySelect(event) {
    event.onComplete = function(event) {
      updateLibraryToolbar();
    }
  }
  
  function handleProjectAction(event) {
    switch(event.target) {
      case "back":
        return history.go(-1);
      case "change":
        return startDeployProject();
      case "jnlp":
        return uploadProjectJNLP();
      case "disable":
        return disableProject(true);
      case "enable":
        return disableProject(false);
      case "delete":
        return deleteProject(true);
      case "restore":
        return deleteProject(false);
      case "save":
        return finishDeployProject();
      case "cancel":
        return cancelDeployProject();
    }
  }
  
  function startDeployProject(force) {
    callServerAjax({
      locker: projectForm,
      lockMsg: "Request deploy...",
      url: "_start",
      type: "POST",
      headers: {
        "forceDeploy": force
      }
    }).success(function(data, status, xhr) {
      if (data) {
        projectForm.httpHeaders.deployTask = data;
        updateDeployState(true);
      } else {
        w2uiConfirm({
          msg: "The project already in deploy, forced new deploy?",
          title: "Force Deploy"
        }).yes(function() { startDeployProject(true);  });
      }
    });
  }
  
  function finishDeployProject() {
    var msg;
    if (projectForm.record.deleted)
      msg = "The project will be removed permanently, continue?";
    else if (hasDeletedLibrary())
      msg = "The library marked delete will be removed permanently, continue?";
    else
      msg = "Save project changes now?";
    w2uiConfirm({
      msg: msg,
      title: "Save Deploy"
    }).yes(function() {
      callServerAjax({
        locker: projectForm,
        lockMsg: "Request save...",
        url: "_finish",
        type: "POST"
      }).success(function(data, status, xhr) {
        clearDeployTask();
        reloadProject(false);
      });
    });
  }
  
  function hasDeletedLibrary() {
    for (var i=0; i<libraryGrid.records.length; i++) {
      var library = libraryGrid.records[i];
      library = mergeW2uiGridChanges(library);
      if (library.deleted) return true;
    }
    return false;
  }
  
  function cancelDeployProject() {
    w2uiConfirm({
      msg: "Are you sure to cancel deploy?",
      title: "Cancel Deploy"
    }).yes(function() {
      cancelDeployInternal()
      .success(function(data, status, xhr) {
        clearDeployTask();
        reloadProject(false);
      });
    });
  }
  
  function cancelDeployInternal() {
    return callServerAjax({
      locker: projectForm,
      lockMsg: "Request cancel...",
      url: "_cancel", type: "POST" });
  }

  function clearDeployTask() {
    delete projectForm.httpHeaders.deployTask;    
  }
  
  function uploadProjectJNLP() {
    $("#jnlpUploader").off("change");
    $("#jnlpUploader").val(null);
    $("#jnlpUploader").change(function(event) {
      var project = projectForm.record.name;
      var jnlp = event.target.files[0];
      if (jnlp.name.startsWith(project))
        readAndUploadJNLP(jnlp);
      else {
        w2uiConfirm({
          msg: "File name not match the project, continue?",
          title: jnlp.name
        }).yes(function(){ readAndUploadJNLP(jnlp)});
      } 
    });
    $("#jnlpUploader").trigger("click");
  }
  
  function readAndUploadJNLP(jnlpFile) {
    var reader = new FileReader();
    reader.onload = function(event) {
      var name = jnlpFile.name;
      var info = parseNameVersion(name);
      projectForm.record.version = info[1];
      updateProject(this.result);
    };
    reader.readAsText(jnlpFile, "UTF-8");
  }
  
  function disableProject(disabled) {
    projectForm.record.disabled = disabled;
    updateProject(null);
  }
  
  function deleteProject(deleted) {
    projectForm.record.deleted = deleted;
    updateProject(null);
  }
  
  function updateProject(jnlpContent) {
    callServerAjax({
      locker: projectForm,
      lockMsg: "Updating project...",
      url: "_project",
      type: "POST",
      data: JSON.stringify({
        project: projectForm.record,
        jnlpContent: jnlpContent
      })
    }).success(function(data, status, xhr) {
      projectForm.record = data || {};
      projectForm.refresh();
      updateDeployState(true);
    });
  }
  
  function handleLibraryAction(event) {
    switch(event.target) {
    case "add":
      return requestUploadLibrary();
    case "disable":
      return requestUpdateLibrary({disabled: true});
    case "enable":
      return requestUpdateLibrary({disabled: false});
    case "delete":
      return requestUpdateLibrary({deleted: true});
    case "restore":
      return requestUpdateLibrary({deleted: false});
    case "up":
      return requestMoveLibrary(-1);
    case "down":
      return requestMoveLibrary(+1);
    }
  }
  
  function requestUploadLibrary() {
    $("#jarUploader").off("change");
    $("#jarUploader").val(null);
    $("#jarUploader").change(function(event) {
      showUploadLibraryPopup(this.files);
    });
    $("#jarUploader").trigger("click");
  }
  
  function showUploadLibraryPopup(files) {
    var libraries = [];
    for (var i=0; i<files.length; i++) {
      var fileName = files[i].name;
      var fileSize = files[i].size;
      var info = parseNameVersion(fileName);
      libraries.push({
        fileName: fileName,
        fileSize: fileSize,
        name: info[0],
        version: info[1],
        file: files[i]
      });
    }
    w2popup.open({
      title: deployUtils.lang("Add Libraries"),
      body   : "<div style='display: flex; flex-direction: column; height: 100%; padding-top: 7px;'>"
             + "<div style='display: none'>"
             + deployUtils.lang("Please check and correct library name and version before upload")
             + "</div>"
             + "<div id='uploadLibsGrid' style='margin-top: 10px; flex-grow: 1'></div></div>",
      buttons: "<button class='w2ui-btn' id='btnUploadLibs'>" + deployUtils.lang("Upload") + "</button>"
             + "<button class='w2ui-btn' onclick='w2popup.close();'>" + deployUtils.lang("Cancel") + "</button>",
      width  : 600,
      height : 400,
      modal  : true,
      onOpen : function(event) {
        event.libraries = libraries;
        event.onComplete = initUploadLibraryPopup;
      },
      onClose: function(event) {
        if (uploadLibsGrid) {
          uploadLibsGrid.destroy();
          delete uploadLibsGrid;
        }
      }
    })
  }
  
  function initUploadLibraryPopup(event) {
    $("#uploadLibsGrid").prev().show();
    uploadLibsGrid = $("#uploadLibsGrid").w2grid({
      name: "uploadLibsGrid",
      recid: "fileName",
      records: event.libraries,
      columns: [
        {field: "status", size: "30px", render: renderLibUploadStatus},
        {field: "fileName", caption: deployUtils.lang("File Name"), size: "30%"},
        {field: "fileSize", caption: deployUtils.lang("File Size"), size: "15%", render: renderLibrarySize},
        {field: "name", caption: deployUtils.lang("Library Name"), size: "30%", editable: {type: "text"} },
        {field: "version", caption: deployUtils.lang("Version"), size: "20%", editable: {type: "text"} },
      ],
      onClick: function(event) {
        if (!event.column || !event.recid) return;
        var column = uploadLibsGrid.columns[event.column];
        if (column.field == "name" || column.field == "version")
          uploadLibsGrid.editField(event.recid, event.column);
      }
    });
    $("#btnUploadLibs").off("click").click(function() {
      w2popup.lock(deployUtils.lang("Preparing upload..."))
      loopUploadLibrary(0);
    });
  }
  
  function renderLibUploadStatus(uploading, index, column) {
    var icon_class = 'jnlp-w2cell-icon ';
    if (uploading.status == 'uploaded')
      icon_class += 'icon-deploy-accept';
    else if (uploading.status == 'failed')
      icon_class += 'icon-deploy-cancel';
    else
      return;
    return "<span class='" + icon_class + "'></span>";
  }

  function loopUploadLibrary(index) {
    var libraries = uploadLibsGrid.records;
    if (index >= libraries.length) {
      return checkUploadLibraryFinished();
    }
    var library = libraries[index];
    if (library.status == "uploaded") {
      loopUploadLibrary(index + 1);
    } else {
      var msg = deployUtils.lang("Uploading:");
      w2popup.lock(msg + " " + library.fileName);
      var reader = new FileReader();
      reader.onload = function(event) {
        performUploadLibrary(index, this.result);
      }
      reader.readAsDataURL(library.file);
    }
  }
  
  function performUploadLibrary(index, jarContent) {
    var library = uploadLibsGrid.records[index];
    library = mergeW2uiGridChanges(library);
    var uploading = libraryGrid.get(library.name);
    uploading = mergeW2uiGridChanges(uploading);
    $.extend(uploading, {
      name: library.name,
      version: library.version
    });
    delete uploading.w2ui; // modules are unneeded
    if (!uploading.sequence) {
      var count = libraryGrid.records.length;
      var last = libraryGrid.records[count -1];
      last = mergeW2uiGridChanges(last);
      var sequence = last && last.sequence || 0;
      uploading.sequence = sequence + 1;
    }
    callServerAjax({
      url: "_lib",
      type: "POST",
      data: JSON.stringify({
        library: uploading,
        jarContent: jarContent
      }),
      success: function(data, status, xhr) {
        library.status = "uploaded";
        delete library.w2ui;
        uploadLibsGrid.set(library.fileName, library);
        if (!libraryGrid.records.length)
          data.major = true;
        performUpdateLibrary(data, true);
        loopUploadLibrary(index + 1);
      },
      error: function(xhr, status, msg) {
        var failed = uploadLibsGrid.records[index];
        failed.status = "failed";
        uploadLibsGrid.set(failed.fileName, failed);
        showUploadLibraryError(index, xhr, msg);
      }
    });
  }
 
  function collapseResetChildren(libraryName) {
    var library = libraryGrid.get(libraryName);
    if (library) {
      if (library.w2ui && library.w2ui.expanded)
        libraryGrid.collapse(libraryName);
      resetLibraryChildren(library);
    }
  }
  
  function showUploadLibraryError(index, xhr, msg) {
    var fileName = uploadLibsGrid.records[index].fileName;
    var action = "abort";
    w2popup.message({
      body   : "<div style='height: 100%; padding: 7px; display: flex; flex-direction: column;'>"
             + "<div>" + fileName + " " + deployUtils.lang("Upload failed:") + " " + msg + "</div>"
             + "<div style='flex-grow: 1; overflow: scroll'>" + xhr.responseText + "</div>"
             + "</div>",
      buttons: "<button id='btnRetryUploadLib' class='w2ui-btn'>" + deployUtils.lang("Retry") + "</button>"
             + "<button id='btnIgnoreUploadLib' class='w2ui-btn'>" + deployUtils.lang("Ignore") + "</button>"
             + "<button onclick='w2popup.message()' class='w2ui-btn'>" + deployUtils.lang("Abort") + "</button>",
      width  : 500, height: 300,
      onClose: function() {
        if (action == "retry")
          loopUploadLibrary(index);
        else if (action == "ignore")
          loopUploadLibrary(index + 1);
      }
    });
    $("#btnRetryUploadLib").off("click").click(function() {
      action = "retry";
      w2popup.message();
    });
    $("#btnIgnoreUploadLib").off("click").click(function() {
      action = "ignore";
      w2popup.message();
    });
  }

  function checkUploadLibraryFinished() {
    for (var i=0; i<uploadLibsGrid.records.length; i++) {
      if (uploadLibsGrid.records[i].status != "uploaded") return;
    }
    w2popup.message({
      body   : "<div class='w2ui-centered'>" + deployUtils.lang("Upload finished") + "</div>",
      buttons: "<button class='w2ui-btn' onclick='w2popup.close();'>" + deployUtils.lang("Close") + "</button>",
      width: 300, height: 150 });
  }

  function requestUpdateLibrary(options) {
    var name = libraryGrid.getSelection()[0];
    var library = libraryGrid.get(name);
    library = mergeW2uiGridChanges(library);
    var process = $.extend(library, options);
    delete process.w2ui;
    callServerAjax({
      locker: libraryGrid,
      lockMsg: "Updating library...",
      url: "_lib",
      type: "POST",
      data: JSON.stringify({
        library: process
      })
    }).success(function(data, status, xhr) {
      performUpdateLibrary(data);
      updateLibraryToolbar();
    });
  }
  
  function requestMoveLibrary(direction) {
    var count = libraryGrid.records.length;
    var from = libraryGrid.getSelection(true)[0];
    var source = libraryGrid.records[from];
    source = mergeW2uiGridChanges(source);
    var to = from, target;
    do {
      to = to + direction;
      if (to < 0 || to >= count) return;
      target = libraryGrid.records[to];
      target = mergeW2uiGridChanges(target);
    } while(!isLibraryRecord(target));
    libraryGrid.collapse(source.name);
    libraryGrid.collapse(target.name);
    var temp = source.sequence;
    source.sequence = target.sequence;
    target.sequence = temp;
    source.major = to == 0;
    target.major = from == 0;
    performMoveLibrary(source, target, 1);
  }
  
  function performMoveLibrary(source, target, step) {
    var updating = step == 1 ? source : target;
    if (step == 1) {
      libraryGrid.lock(deployUtils.lang("Updating library..."));
    }
    callServerAjax({
      url: "_lib",
      type: "POST",
      data: JSON.stringify({
        library: updating
      })
    }).success(function(data, status, xhr) {
      performUpdateLibrary(data);
      if (step == 1)
        performMoveLibrary(source, target, 2);
      else {
        libraryGrid.unlock();
        swapLibraryPosition(source, target);
      }
    });
  }
  
  function performUpdateLibrary(updated, reset) {
    var existed = libraryGrid.get(updated.name);
    var library = existed || {};
    var w2ui = library.w2ui || {};
    w2ui.changes = w2ui.changes || {};
    for (var field in updated) {
      var v1 = library[field] || false;
      var v2 = updated[field] || false;
      if (v1 == v2)
        delete w2ui.changes[field];
      else
        w2ui.changes[field] = updated[field];
    }
    library.recid = updated.name;
    library.w2ui = w2ui;
    if (!existed) {
      libraryGrid.add(library);
    } else {
      if (reset && library.w2ui.expanded) {
        libraryGrid.collapse(library.recid);
      }
      libraryGrid.set(library.recid, library);
    }
    if (reset) {
      resetLibraryChildren(library);
      libraryGrid.refreshRow(library.recid);
    }
  }
  
  function swapLibraryPosition(source, target) {
    var index1 = libraryGrid.get(source.name, true);
    var index2 = libraryGrid.get(target.name, true);
    source = libraryGrid.records[index1];
    target = libraryGrid.records[index2];
    libraryGrid.records[index1] = target;
    libraryGrid.records[index2] = source;
    libraryGrid.refresh();
    libraryGrid.select(source.recid);
  }
  
  function updateDeployState(deploying) {
    var initial = projectForm.record.initial || false;
    var disabled = projectForm.record.disabled || false;
    var deleted = projectForm.record.deleted || false;
    projectForm.toolbar.set("back", {disabled: deploying});
    projectForm.toolbar.set("change", {disabled: deploying});
    projectForm.toolbar.set("jnlp", {disabled: !deploying || disabled || deleted});
    projectForm.toolbar.set("disable", {disabled: !deploying || initial || disabled || deleted, hidden: disabled});
    projectForm.toolbar.set("enable", {disabled: !deploying || initial || !disabled || deleted, hidden: !disabled});
    projectForm.toolbar.set("delete", {disabled: !deploying || initial || deleted, hidden: deleted});
    projectForm.toolbar.set("restore", {disabled: !deploying || initial || !deleted, hidden: !deleted})
    projectForm.toolbar.set("save", {disabled: !deploying || initial});
    projectForm.toolbar.set("cancel", {disabled: !deploying});
    var $nameField = projectForm.get("name").$el;
    if (deleted)
      $nameField.w2tag(deployUtils.lang("Project will be deleted"));
    else if (disabled && projectForm.origin.disabled)
      $nameField.w2tag(deployUtils.lang("Project has been disabled"));
    else if (disabled)
      $nameField.w2tag(deployUtils.lang("Project will be disabled"));
    else
      $nameField.w2tag();
    var jnlpButton = $("#tb_projectForm_toolbar_item_jnlp");
    if (deploying && initial)
      jnlpButton.w2tag(deployUtils.lang("Click to upload JNLP file"));
    else
      jnlpButton.w2tag();
    if (initial)
      projectForm.toolbar.set("jnlp", {text: deployUtils.lang("Upload JNLP")});
    else 
      projectForm.toolbar.set("jnlp", {text: deployUtils.lang("Update JNLP")});
    if (!deploying) {
      $("#icon_default").off("click");
      $("#icon_shortcut").off("click");
      $("#icon_splash").off("click");
    } else {
      $("#icon_default").click(uploadProjectIcon);
      $("#icon_shortcut").click(uploadProjectIcon);
      $("#icon_splash").click(uploadProjectIcon);
    }

    var readonly = !deploying || initial || disabled || deleted;
    setFieldReadonly(projectForm.get("version"), readonly);
    setFieldReadonly(projectForm.get("title"), readonly);
    setFieldReadonly(projectForm.get("moduleBaseClass"), readonly);
    setFieldReadonly(projectForm.get("moduleNameGetter"), readonly);
    setFieldReadonly(projectForm.get("moduleVersionGetter"), readonly);
    updateLibraryToolbar();
  }
  
  function setFieldReadonly(field, readonly) {
    if (readonly)
      field.$el.attr("readonly", "");
    else
      field.$el.removeAttr("readonly");
  }
  
  function updateLibraryToolbar() {
    var display = projectForm.toolbar.get("save").disabled;
    var disabled = projectForm.record.disabled || false;
    var deleted = projectForm.record.deleted || false;
    var readonly = display || disabled  || deleted;
    libraryGrid.toolbar.set("add", {disabled: readonly});
    var name = libraryGrid.getSelection()[0];
    var library = mergeW2uiGridChanges(libraryGrid.get(name));
    var isLibrary = isLibraryRecord(library);
    libraryGrid.toolbar.set("delete", {disabled: readonly || !isLibrary});
    var libDisabled = library.disabled;
    var libDeleted  = library.deleted;
    libraryGrid.toolbar.set("disable", {disabled: readonly || libDisabled, hidden: libDisabled});
    libraryGrid.toolbar.set("enable", {disabled: readonly || !libDisabled, hidden: !libDisabled});
    libraryGrid.toolbar.set("delete", {disabled: readonly || libDeleted, hidden: libDeleted});
    libraryGrid.toolbar.set("restore", {disabled: readonly || !libDeleted, hidden: !libDeleted});
    libraryGrid.toolbar.set("up", {disabled: readonly || !isLibrary});
    libraryGrid.toolbar.set("down", {disabled: readonly || !isLibrary});
  }
  
  function parseNameVersion(fileName) {
    fileName = fileName.replace(/\.[^\.]+$/, "");
    var match  = fileName.match(/(.*)(-(\d.*))/);
    var name = match ? match[1] : fileName;
    return [name, match ? match[3] : null];
  }
  
  function uploadProjectIcon() {
    $("#iconUploader").off("change");
    $("#iconUploader").val(null);
    var div = $(this);
    $("#iconUploader").change(function(event) {
      readUploadIcon(div, this.files[0]);
    });
    $("#iconUploader").trigger("click");
  }
  
  function readUploadIcon(div, icon) {
    var reader = new FileReader();
    reader.onload = function(event) {
      performUploadIcon(div, this.result);
    }
    reader.readAsDataURL(icon);
  }
  
  function performUploadIcon(div, iconContent) {
    var kind = div.attr("id").substring(5);
    callServerAjax({
      locker: projectForm,
      lockMsg: "Updating icon...",
      url: "_icons",
      type: "POST",
      data: JSON.stringify({
        kind: kind,
        iconContent: iconContent
      })
    }).success(function(data, status, xhr) {
      div.css("background-image", "url('" + iconContent + "')");
    });
  }
  
  function cleanDeployPage() {
    if (projectForm && projectForm.httpHeaders.deployTask) cancelDeployInternal();
  }
  
  function callServerAjax(callOptions) {
    if (callOptions.locker) {
      var msg = callOptions.lockMsg;
      msg = deployUtils.lang(msg);
      callOptions.locker.lock(msg);
    }
    var ajaxOptions = $.extend(true, {
      type: "GET", dataType: "JSON",
      complete: function(data, staus, xhr) {
        if (callOptions.locker)
          callOptions.locker.unlock();
      },
      error: function(xhr, status, msg) {
        showServerError(msg, xhr.responseText);
      }
    }, callOptions, {
      headers: {
        "deployTask": projectForm.httpHeaders.deployTask
      }
    });
    delete ajaxOptions.locker;
    delete ajaxOptions.lockMsg;
    return $.ajax(ajaxOptions);
  }
  
  function showServerError(title, body) {
    w2popup.open({
      title: title,  body: body,
      modal: true,
      width: 600,    height: 400
    });
  }
  
  function w2uiConfirm(options) {
    return w2confirm({
      yes_text: deployUtils.lang("Yes"),
      no_text : deployUtils.lang("No"),
      msg     : deployUtils.lang(options.msg),
      title   : deployUtils.lang(options.title)
    });
  }
  
  function formatBytes(bytes, decimals) {
    if(bytes == 0) return '0 Bytes';
    var k = 1024,
        dm = decimals || 0,
        sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'],
        i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
  }
})(jQuery);
deployUtils.locale();
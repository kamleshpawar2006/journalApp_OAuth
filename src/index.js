const fs = require('fs');
const path = require('path');
const readline = require('readline');

const sourceFolder = './main';
const mergedOutputFile = './kamlesh/all-code-merged.txt';
const recreateOutputFolder = './kamlesh/restored';
const excludedFiles = ['package-lock.json', '.DS_Store'];
const outputFile = './kamlesh/file-list.txt';
const excludedDirs = ['repositories', 'resources' ,'service', 'entity' , 'Angular-Code', 'target', '.git', 'kamlesh', 'mvnw.cmd', 'mvnw', 'logs', '.mvn', '.idea', '.gitignore'];

function getAllFilesRecursively(dir) {
  let results = [];
  const entries = fs.readdirSync(dir, { withFileTypes: true });

  for (const entry of entries) {
    const fullPath = path.join(dir, entry.name);
    if (entry.isDirectory()) {
        if (!excludedDirs.includes(entry.name)) {
            results = results.concat(getAllFilesRecursively(fullPath));
         }
    } else if (!excludedFiles.includes(entry.name)) {
      results.push(fullPath);
    }
  }

  return results;
}

function extractCodeFromFilesToSingleFile(filePaths = null) {

  const outputDir = path.dirname(mergedOutputFile);
  fs.mkdirSync(outputDir, { recursive: true }); // Ensure folder exists
  
  const files = filePaths || getAllFilesRecursively(sourceFolder);
  let output = '';

  files.forEach(filePath => {
    const content = fs.readFileSync(filePath, 'utf8');
    const relativePath = path.relative('.', filePath);

    output += `--- START: ${relativePath} ---\n`;
    output += content + '\n';
    output += `--- END: ${relativePath} ---\n\n`;

    console.log(`Extracted: ${relativePath}`);
  });

  fs.writeFileSync(mergedOutputFile, output, 'utf8');
  console.log(`Done! Extracted ${files.length} files into ${mergedOutputFile}`);
}

function recreateFilesFromMergedFile() {
  const content = fs.readFileSync(mergedOutputFile, 'utf8');
  const fileBlocks = content.split(/--- START: (.*?) ---\n([\s\S]*?)--- END: \1 ---/g);

  for (let i = 1; i < fileBlocks.length; i += 3) {
    const filePath = fileBlocks[i].trim();
    const fileContent = fileBlocks[i + 1];

    if (excludedFiles.includes(path.basename(filePath))) {
      continue;
    }

    const finalPath = path.join(recreateOutputFolder, filePath);
    const folderPath = path.dirname(finalPath);

    fs.mkdirSync(folderPath, { recursive: true });
    fs.writeFileSync(finalPath, fileContent, 'utf8');

    console.log(`Created: ${finalPath}`);
  }

  console.log(`All files recreated in ${recreateOutputFolder}`);
}

function logAllFileNames(dir, allFiles = []) {
  const entries = fs.readdirSync(dir, { withFileTypes: true });

  entries.forEach(entry => {
    const fullPath = path.join(dir, entry.name);

    if (entry.isDirectory()) {
      if (!excludedDirs.includes(entry.name)) {
        logAllFileNames(fullPath, allFiles); // Recurse
      }
    } else if (!excludedFiles.includes(entry.name)) {
      const relativePath = path.relative('.', fullPath);
      allFiles.push(relativePath);
    }
  });

  return allFiles;
}

function generateFileList() {
  const outputDir = path.dirname(outputFile);
  fs.mkdirSync(outputDir, { recursive: true });
  const files = logAllFileNames(sourceFolder);

  // Write to output file
  fs.writeFileSync(outputFile, files.join('\n'), 'utf8');

  // Log to console
  console.log(`Saved ${files.length} file paths to ${outputFile}`);
  files.forEach(file => console.log(file));
}

// extractCodeFromFilesToSingleFile([
//   "src/app/pages/journal/journal-list/journal-list.component.ts"
// ]);
// extractCodeFromFilesToSingleFile();
// recreateFilesFromMergedFile();
// generateFileList();

let filesToBeEstracted = [
  "src/app/pages/journal/journal-store/journal.actions.ts",
  "src/app/pages/journal/journal-store/journal.effect.ts",
  "src/app/pages/journal/journal-store/journal.reducer.ts",
  "src/app/pages/journal/journal-store/journal.selector.ts",
  "src/app/pages/journal/journal-list/journal-list.component.ts",
  "src/app/pages/journal/journal-layout/journal-layout.component.ts",
  "src/app/pages/journal/Journal.model.ts",
  "src/app/pages/journal/journal.service.ts",
  "src/app/app.config.ts",
  "src/main.ts"
]


// Interactive menu
const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout
});

console.log("\nSelect an action:");
console.log("1 - Extract code from files into a single file");
console.log("2 - Recreate files from the merged file");
console.log("3 - Generate a file list");
console.log("4 - Exit");

rl.question("\nEnter the number of the action you want to perform: ", (answer) => {
  switch (answer.trim()) {
    case '1':
      extractCodeFromFilesToSingleFile();
      break;
    case '2':
      recreateFilesFromMergedFile();
      break;
    case '3':
      generateFileList();
      break;
    case '4':
      console.log("Exiting...");
      break;
    default:
      console.log("Invalid choice. Please enter a valid number.");
  }
  rl.close();
});
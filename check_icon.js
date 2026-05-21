const fs = require('fs');
const path = 'C:/Users/koresheff/AndroidStudioProjects/PetAdopt/icon.png';
try {
    const stats = fs.statSync(path);
    console.log('Size:', stats.size);
} catch(e) {
    console.log('Error:', e.message);
}

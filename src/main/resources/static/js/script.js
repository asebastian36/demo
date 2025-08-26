// script.js simplificado
document.addEventListener('DOMContentLoaded', function() {
    // Manejar cambio de archivo
    const txtFile = document.getElementById('txtFile');
    if (txtFile) {
        txtFile.addEventListener('change', function(e) {
            const fileName = e.target.files[0] ? e.target.files[0].name : 'Seleccionar archivo .txt';
            const fileNameElement = document.getElementById('file-name');
            if (fileNameElement) {
                fileNameElement.textContent = fileName;
            }
        });
    }

    // Mostrar loading al enviar el formulario
    const uploadForm = document.getElementById('uploadForm');
    if (uploadForm) {
        uploadForm.addEventListener('submit', function() {
            const loadingElement = document.getElementById('loading');
            if (loadingElement) {
                loadingElement.style.display = 'block';
            }
        });
    }

    // Manejar la selección visual de opciones
    const optionButtons = document.querySelectorAll('.option-btn');
    if (optionButtons.length > 0) {
        optionButtons.forEach(btn => {
            btn.addEventListener('click', function() {
                optionButtons.forEach(b => b.classList.remove('selected'));
                this.classList.add('selected');
                this.querySelector('input[type="radio"]').checked = true;
            });
        });

        // Seleccionar la primera opción por defecto
        optionButtons[0].classList.add('selected');
    }
});
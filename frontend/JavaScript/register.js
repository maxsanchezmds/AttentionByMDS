// Wait for DOM to be fully loaded
document.addEventListener('DOMContentLoaded', function() {
    console.log('Register page loaded');

    // DOM Elements
    const typeButtons = document.querySelectorAll('.type-button');
    const registerForms = document.querySelectorAll('.register-form');
    const empresaForm = document.getElementById('empresaForm');
    const ejecutivoForm = document.getElementById('ejecutivoForm');

    // Password toggles
    const passwordToggleAdmin = document.getElementById('passwordToggleAdmin');
    const passwordToggleEjec = document.getElementById('passwordToggleEjec');
    const passwordInputAdmin = document.getElementById('passwordAdmin');
    const passwordInputEjec = document.getElementById('passwordEjec');

    // Form buttons
    const empresaButton = document.getElementById('empresaButton');
    const ejecutivoButton = document.getElementById('ejecutivoButton');

    console.log('Elements found:', {
        typeButtons: typeButtons.length,
        registerForms: registerForms.length,
        empresaForm: !!empresaForm,
        ejecutivoForm: !!ejecutivoForm
    });

    // Account Type Switching
    typeButtons.forEach(button => {
        button.addEventListener('click', () => {
            console.log('Type button clicked:', button.dataset.type);
            const type = button.dataset.type;
            
            // Update active button
            typeButtons.forEach(btn => btn.classList.remove('active'));
            button.classList.add('active');
            
            // Show corresponding form
            registerForms.forEach(form => {
                form.classList.remove('active');
                if (form.dataset.type === type) {
                    form.classList.add('active');
                    console.log('Form activated:', type);
                }
            });
        });
    });

    // Password Toggle Functionality
    function setupPasswordToggle(toggleButton, passwordInput) {
        if (!toggleButton || !passwordInput) return;
        
        toggleButton.addEventListener('click', () => {
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);
            
            if (type === 'password') {
                toggleButton.classList.remove('hide');
                toggleButton.classList.add('show');
            } else {
                toggleButton.classList.remove('show');
                toggleButton.classList.add('hide');
            }
        });
    }

    setupPasswordToggle(passwordToggleAdmin, passwordInputAdmin);
    setupPasswordToggle(passwordToggleEjec, passwordInputEjec);

    // Validation Functions
    function validateEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }

    function validatePhone(phone) {
        const phoneRegex = /^[\+]?[\d\s\-\(\)]{8,}$/;
        return phoneRegex.test(phone);
    }

    function validateNIT(nit) {
        const nitRegex = /^[\d\-]{8,}$/;
        return nitRegex.test(nit);
    }

    function validatePassword(password) {
        return password && password.length >= 8;
    }

    function validateRequired(value) {
        return value && value.trim().length > 0;
    }

    function validateUUID(uuid) {
        const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
        return uuidRegex.test(uuid);
    }

    // Real-time Validation Setup
    function setupRealTimeValidation(inputId, validationFunction, errorId, errorMessage = 'Campo inválido') {
        const input = document.getElementById(inputId);
        const errorElement = document.getElementById(errorId);
        
        if (!input || !errorElement) return;
        
        input.addEventListener('input', () => {
            const isValid = validationFunction(input.value);
            
            if (input.value && !isValid) {
                input.classList.add('error');
                errorElement.classList.add('show');
                errorElement.textContent = errorMessage;
            } else {
                input.classList.remove('error');
                errorElement.classList.remove('show');
            }
        });
        
        input.addEventListener('blur', () => {
            if (input.hasAttribute('required') && !validateRequired(input.value)) {
                input.classList.add('error');
                errorElement.classList.add('show');
                errorElement.textContent = 'Este campo es obligatorio';
            }
        });
    }

    // Setup validations for empresa form
    setupRealTimeValidation('nombreLegalEmpresa', validateRequired, 'nombreLegalEmpresaError', 'Este campo es obligatorio');
    setupRealTimeValidation('nitEmpresa', validateNIT, 'nitEmpresaError', 'Ingresa un NIT válido');
    setupRealTimeValidation('telefonoEmpresa', validatePhone, 'telefonoEmpresaError', 'Ingresa un teléfono válido');
    setupRealTimeValidation('telefonoWhatsapp', validatePhone, 'telefonoWhatsappError', 'Ingresa un WhatsApp válido');
    setupRealTimeValidation('correoEmpresa', validateEmail, 'correoEmpresaError', 'Ingresa un correo válido');
    setupRealTimeValidation('direccionEmpresa', validateRequired, 'direccionEmpresaError', 'Este campo es obligatorio');
    setupRealTimeValidation('nombreCompletoAdmin', validateRequired, 'nombreCompletoAdminError', 'Este campo es obligatorio');
    setupRealTimeValidation('correoElectronicoAdmin', validateEmail, 'correoElectronicoAdminError', 'Ingresa un correo válido');
    setupRealTimeValidation('telefonoAdmin', validatePhone, 'telefonoAdminError', 'Ingresa un teléfono válido');
    setupRealTimeValidation('documentoIdentidadAdmin', validateRequired, 'documentoIdentidadAdminError', 'Este campo es obligatorio');
    setupRealTimeValidation('passwordAdmin', validatePassword, 'passwordAdminError', 'La contraseña debe tener al menos 8 caracteres');

    // Setup validations for ejecutivo form
    setupRealTimeValidation('nombreCompletoEjec', validateRequired, 'nombreCompletoEjecError', 'Este campo es obligatorio');
    setupRealTimeValidation('correoElectronicoEjec', validateEmail, 'correoElectronicoEjecError', 'Ingresa un correo válido');
    setupRealTimeValidation('telefonoEjec', validatePhone, 'telefonoEjecError', 'Ingresa un teléfono válido');
    setupRealTimeValidation('documentoIdentidadEjec', validateRequired, 'documentoIdentidadEjecError', 'Este campo es obligatorio');
    setupRealTimeValidation('passwordEjec', validatePassword, 'passwordEjecError', 'La contraseña debe tener al menos 8 caracteres');
    setupRealTimeValidation('identificadorEmpresa', validateUUID, 'identificadorEmpresaError', 'Ingresa un ID de empresa válido');

    // Country selection validation
    const paisEmpresa = document.getElementById('paisEmpresa');
    const paisError = document.getElementById('paisEmpresaError');
    if (paisEmpresa && paisError) {
        paisEmpresa.addEventListener('change', () => {
            if (paisEmpresa.value) {
                paisEmpresa.classList.remove('error');
                paisError.classList.remove('show');
            } else {
                paisEmpresa.classList.add('error');
                paisError.classList.add('show');
            }
        });
    }

    // Form Submission Functions
    function validateFormEmpresa() {
        const requiredFields = [
            { id: 'nombreLegalEmpresa', validator: validateRequired, error: 'nombreLegalEmpresaError' },
            { id: 'nitEmpresa', validator: validateNIT, error: 'nitEmpresaError' },
            { id: 'telefonoEmpresa', validator: validatePhone, error: 'telefonoEmpresaError' },
            { id: 'telefonoWhatsapp', validator: validatePhone, error: 'telefonoWhatsappError' },
            { id: 'correoEmpresa', validator: validateEmail, error: 'correoEmpresaError' },
            { id: 'direccionEmpresa', validator: validateRequired, error: 'direccionEmpresaError' },
            { id: 'paisEmpresa', validator: validateRequired, error: 'paisEmpresaError' },
            { id: 'nombreCompletoAdmin', validator: validateRequired, error: 'nombreCompletoAdminError' },
            { id: 'correoElectronicoAdmin', validator: validateEmail, error: 'correoElectronicoAdminError' },
            { id: 'telefonoAdmin', validator: validatePhone, error: 'telefonoAdminError' },
            { id: 'documentoIdentidadAdmin', validator: validateRequired, error: 'documentoIdentidadAdminError' },
            { id: 'passwordAdmin', validator: validatePassword, error: 'passwordAdminError' }
        ];
        
        let isValid = true;
        
        requiredFields.forEach(field => {
            const input = document.getElementById(field.id);
            const errorElement = document.getElementById(field.error);
            
            if (!input || !errorElement) return;
            
            const value = input.value;
            const fieldValid = field.validator(value);
            
            if (!fieldValid) {
                input.classList.add('error');
                errorElement.classList.add('show');
                isValid = false;
            } else {
                input.classList.remove('error');
                errorElement.classList.remove('show');
            }
        });
        
        return isValid;
    }

    function validateFormEjecutivo() {
        const requiredFields = [
            { id: 'nombreCompletoEjec', validator: validateRequired, error: 'nombreCompletoEjecError' },
            { id: 'correoElectronicoEjec', validator: validateEmail, error: 'correoElectronicoEjecError' },
            { id: 'telefonoEjec', validator: validatePhone, error: 'telefonoEjecError' },
            { id: 'documentoIdentidadEjec', validator: validateRequired, error: 'documentoIdentidadEjecError' },
            { id: 'passwordEjec', validator: validatePassword, error: 'passwordEjecError' },
            { id: 'identificadorEmpresa', validator: validateUUID, error: 'identificadorEmpresaError' }
        ];
        
        let isValid = true;
        
        requiredFields.forEach(field => {
            const input = document.getElementById(field.id);
            const errorElement = document.getElementById(field.error);
            
            if (!input || !errorElement) return;
            
            const value = input.value;
            const fieldValid = field.validator(value);
            
            if (!fieldValid) {
                input.classList.add('error');
                errorElement.classList.add('show');
                isValid = false;
            } else {
                input.classList.remove('error');
                errorElement.classList.remove('show');
            }
        });
        
        return isValid;
    }

    function collectEmpresaData() {
        return {
            nombreLegalEmpresa: document.getElementById('nombreLegalEmpresa').value.trim(),
            nitEmpresa: document.getElementById('nitEmpresa').value.trim(),
            telefonoEmpresa: document.getElementById('telefonoEmpresa').value.trim(),
            correoEmpresa: document.getElementById('correoEmpresa').value.trim(),
            direccionEmpresa: document.getElementById('direccionEmpresa').value.trim(),
            paisEmpresa: document.getElementById('paisEmpresa').value,
            telefonoWhatsapp: document.getElementById('telefonoWhatsapp').value.trim(),
            nombreCompleto: document.getElementById('nombreCompletoAdmin').value.trim(),
            correoElectronico: document.getElementById('correoElectronicoAdmin').value.trim(),
            telefono: document.getElementById('telefonoAdmin').value.trim(),
            documentoIdentidad: document.getElementById('documentoIdentidadAdmin').value.trim(),
            password: document.getElementById('passwordAdmin').value
        };
    }

    function collectEjecutivoData() {
        return {
            nombreCompleto: document.getElementById('nombreCompletoEjec').value.trim(),
            telefono: document.getElementById('telefonoEjec').value.trim(),
            correoElectronico: document.getElementById('correoElectronicoEjec').value.trim(),
            documentoIdentidad: document.getElementById('documentoIdentidadEjec').value.trim(),
            password: document.getElementById('passwordEjec').value,
            identificadorEmpresa: document.getElementById('identificadorEmpresa').value.trim()
        };
    }

    // Loading state management
    function setLoadingState(button, isLoading) {
        if (isLoading) {
            button.classList.add('loading');
            button.textContent = '';
            button.disabled = true;
        } else {
            button.classList.remove('loading');
            button.disabled = false;
            // Reset text based on form type
            if (button.id === 'empresaButton') {
                button.textContent = 'Registrar Empresa';
            } else {
                button.textContent = 'Registrar Ejecutivo';
            }
        }
    }

    // Form Submission Handlers
    if (empresaForm) {
        empresaForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            
            if (!validateFormEmpresa()) {
                // Scroll to first error
                const firstError = document.querySelector('.form-input.error');
                if (firstError) {
                    firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    firstError.focus();
                }
                return;
            }
            
            const empresaData = collectEmpresaData();
            
            // Add loading state
            setLoadingState(empresaButton, true);
            
            try {
                // Simulate API call
                console.log('Datos de empresa a enviar:', empresaData);
                
                // Here you would make the actual API call
                // const response = await fetch('/api/register/empresa', {
                //     method: 'POST',
                //     headers: {
                //         'Content-Type': 'application/json',
                //     },
                //     body: JSON.stringify(empresaData)
                // });
                
                // Simulate loading time
                await new Promise(resolve => setTimeout(resolve, 2000));
                
                // Show success message
                alert('¡Empresa registrada exitosamente! Se ha enviado un correo de confirmación.');
                
                // Reset form or redirect
                // empresaForm.reset();
                // window.location.href = 'login.html';
                
            } catch (error) {
                console.error('Error al registrar empresa:', error);
                alert('Error al registrar la empresa. Por favor, intenta nuevamente.');
            } finally {
                setLoadingState(empresaButton, false);
            }
        });
    }

    if (ejecutivoForm) {
        ejecutivoForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            
            if (!validateFormEjecutivo()) {
                // Scroll to first error
                const firstError = document.querySelector('.form-input.error');
                if (firstError) {
                    firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    firstError.focus();
                }
                return;
            }
            
            const ejecutivoData = collectEjecutivoData();
            
            // Add loading state
            setLoadingState(ejecutivoButton, true);
            
            try {
                // Simulate API call
                console.log('Datos de ejecutivo a enviar:', ejecutivoData);
                
                // Here you would make the actual API call
                // const response = await fetch('/api/register/ejecutivo', {
                //     method: 'POST',
                //     headers: {
                //         'Content-Type': 'application/json',
                //     },
                //     body: JSON.stringify(ejecutivoData)
                // });
                
                // Simulate loading time
                await new Promise(resolve => setTimeout(resolve, 2000));
                
                // Show success message
                alert('¡Ejecutivo registrado exitosamente! Se ha enviado un correo de confirmación.');
                
                // Reset form or redirect
                // ejecutivoForm.reset();
                // window.location.href = 'login.html';
                
            } catch (error) {
                console.error('Error al registrar ejecutivo:', error);
                alert('Error al registrar el ejecutivo. Por favor, intenta nuevamente.');
            } finally {
                setLoadingState(ejecutivoButton, false);
            }
        });
    }

    // Add ripple effect to register buttons
    function addRippleEffect(button) {
        if (!button) return;
        
        button.addEventListener('click', function(e) {
            const ripple = document.createElement('span');
            const rect = this.getBoundingClientRect();
            const size = Math.max(rect.width, rect.height);
            const x = e.clientX - rect.left - size / 2;
            const y = e.clientY - rect.top - size / 2;
            
            ripple.style.cssText = `
                position: absolute;
                border-radius: 50%;
                background: rgba(255, 255, 255, 0.3);
                transform: scale(0);
                animation: ripple 0.6s linear;
                width: ${size}px;
                height: ${size}px;
                left: ${x}px;
                top: ${y}px;
            `;
            
            this.appendChild(ripple);
            
            setTimeout(() => {
                ripple.remove();
            }, 600);
        });
    }

    // Apply ripple effect to both buttons
    addRippleEffect(empresaButton);
    addRippleEffect(ejecutivoButton);

    // Add CSS for ripple animation if not already present
    if (!document.querySelector('#ripple-styles')) {
        const style = document.createElement('style');
        style.id = 'ripple-styles';
        style.textContent = `
            @keyframes ripple {
                to {
                    transform: scale(4);
                    opacity: 0;
                }
            }
        `;
        document.head.appendChild(style);
    }

    // Auto-fill WhatsApp with company phone when company phone changes
    const telefonoEmpresa = document.getElementById('telefonoEmpresa');
    const telefonoWhatsapp = document.getElementById('telefonoWhatsapp');

    if (telefonoEmpresa && telefonoWhatsapp) {
        telefonoEmpresa.addEventListener('input', () => {
            if (!telefonoWhatsapp.value || telefonoWhatsapp.value === telefonoEmpresa.defaultValue) {
                telefonoWhatsapp.value = telefonoEmpresa.value;
            }
        });
    }

    // Prevent form submission on Enter key in password fields (to avoid accidental submission)
    [passwordInputAdmin, passwordInputEjec].forEach(input => {
        if (input) {
            input.addEventListener('keydown', (e) => {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    // Move to next input or submit if it's the last field
                    const form = input.closest('form');
                    const inputs = Array.from(form.querySelectorAll('input, select'));
                    const currentIndex = inputs.indexOf(input);
                    
                    if (currentIndex < inputs.length - 1) {
                        inputs[currentIndex + 1].focus();
                    } else {
                        form.dispatchEvent(new Event('submit'));
                    }
                }
            });
        }
    });

    // Smooth scroll to top when switching between forms
    typeButtons.forEach(button => {
        button.addEventListener('click', () => {
            setTimeout(() => {
                const header = document.querySelector('.register-header');
                if (header) {
                    header.scrollIntoView({ 
                        behavior: 'smooth', 
                        block: 'start' 
                    });
                }
            }, 100);
        });
    });

    console.log('Register page setup complete');
});
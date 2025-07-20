// Simulador de Precios - Funcionalidad
document.addEventListener('DOMContentLoaded', function() {
    
    // Configuración de precios por servicio (por cada 1000 mensajes)
    const servicePrices = {
        sentimentAnalysis: 10,      // $50 por 1k mensajes
        urgencyClassification: 15,  // $30 por 1k mensajes
        aiAssistant: 40            // $80 por 1k mensajes
    };

    // Elementos del simulador
    const messageCountInput = document.getElementById('messageCount');
    const sentimentCheckbox = document.getElementById('sentimentAnalysis');
    const urgencyCheckbox = document.getElementById('urgencyClassification');
    const aiAssistantCheckbox = document.getElementById('aiAssistant');
    const selectedServicesCount = document.getElementById('selectedServicesCount');
    const estimatedTotal = document.getElementById('estimatedTotal');

    // Elementos del dashboard de consumo
    const monthlyConsumption = document.getElementById('monthlyConsumption');
    const monthlyProjection = document.getElementById('monthlyProjection');
    const conversationsProcessed = document.getElementById('conversationsProcessed');
    const averageCost = document.getElementById('averageCost');

    // Función para formatear números a pesos chilenos
    function formatCurrency(amount) {
        return new Intl.NumberFormat('es-CL', {
            style: 'currency',
            currency: 'CLP',
            minimumFractionDigits: 0,
            maximumFractionDigits: 0
        }).format(amount);
    }

    // Función para calcular costos
    function calculateCosts() {
        const messageCount = parseInt(messageCountInput.value) || 0;
        const messageGroups = messageCount / 1000; // Convertir a grupos de 1000

        let totalCost = 0;
        let selectedServices = 0;
        let serviceBreakdown = {
            sentimentAnalysis: 0,
            urgencyClassification: 0,
            aiAssistant: 0
        };

        // Calcular costos por servicio seleccionado
        if (sentimentCheckbox.checked) {
            serviceBreakdown.sentimentAnalysis = messageGroups * servicePrices.sentimentAnalysis;
            totalCost += serviceBreakdown.sentimentAnalysis;
            selectedServices++;
        }

        if (urgencyCheckbox.checked) {
            serviceBreakdown.urgencyClassification = messageGroups * servicePrices.urgencyClassification;
            totalCost += serviceBreakdown.urgencyClassification;
            selectedServices++;
        }

        if (aiAssistantCheckbox.checked) {
            serviceBreakdown.aiAssistant = messageGroups * servicePrices.aiAssistant;
            totalCost += serviceBreakdown.aiAssistant;
            selectedServices++;
        }

        return {
            totalCost,
            selectedServices,
            serviceBreakdown,
            messageCount
        };
    }

    // Función para actualizar el simulador
    function updateSimulator() {
        const costs = calculateCosts();
        
        // Actualizar contador de servicios
        selectedServicesCount.textContent = costs.selectedServices;
        
        // Actualizar total estimado
        estimatedTotal.textContent = formatCurrency(costs.totalCost);
        
        // Actualizar dashboard de consumo
        updateConsumptionDashboard(costs);
    }

    // Función para actualizar el dashboard de consumo
    function updateConsumptionDashboard(costs) {
        const currentMonth = Math.floor(Date.now() / (1000 * 60 * 60 * 24 * 30)) % 12 + 1;
        const daysInMonth = 30;
        const currentDay = Math.floor(Date.now() / (1000 * 60 * 60 * 24)) % daysInMonth + 1;
        
        // Calcular consumo actual (proporción del mes transcurrido)
        const monthProgress = currentDay / daysInMonth;
        const currentConsumption = costs.totalCost * monthProgress;
        
        // Proyección mensual completa
        const projection = costs.totalCost;
        
        // Costo promedio diario
        const dailyAverage = costs.totalCost / daysInMonth;

        // Actualizar valores en el dashboard
        if (monthlyConsumption) {
            monthlyConsumption.textContent = formatCurrency(currentConsumption);
        }
        
        if (monthlyProjection) {
            monthlyProjection.textContent = formatCurrency(projection);
        }
        
        if (conversationsProcessed) {
            // Mostrar número de mensajes procesados
            const processedMessages = Math.floor(costs.messageCount * monthProgress);
            conversationsProcessed.textContent = processedMessages.toLocaleString('es-CL');
        }
        
        if (averageCost) {
            averageCost.textContent = formatCurrency(dailyAverage);
        }

        // Actualizar breakdown de servicios
        updateServiceBreakdown(costs.serviceBreakdown);
        
        // Actualizar barra de progreso de respuestas
        updateResponseProgress(costs.selectedServices);
    }

    // Función para actualizar el breakdown de servicios
    function updateServiceBreakdown(breakdown) {
        const breakdownItems = document.querySelectorAll('.breakdown-item');
        
        if (breakdownItems.length >= 3) {
            // Actualizar Análisis de Conversaciones (Sentimientos)
            const sentimentAmount = breakdownItems[0].querySelector('.breakdown-amount');
            const sentimentBar = breakdownItems[0].querySelector('.breakdown-fill');
            if (sentimentAmount && sentimentBar) {
                sentimentAmount.textContent = formatCurrency(breakdown.sentimentAnalysis);
                const totalCost = Object.values(breakdown).reduce((a, b) => a + b, 0);
                const percentage = totalCost > 0 ? (breakdown.sentimentAnalysis / totalCost) * 100 : 0;
                sentimentBar.style.width = percentage + '%';
            }

            // Actualizar Clasificación de Urgencia  
            const urgencyAmount = breakdownItems[1].querySelector('.breakdown-amount');
            const urgencyBar = breakdownItems[1].querySelector('.breakdown-fill');
            if (urgencyAmount && urgencyBar) {
                urgencyAmount.textContent = formatCurrency(breakdown.urgencyClassification);
                const totalCost = Object.values(breakdown).reduce((a, b) => a + b, 0);
                const percentage = totalCost > 0 ? (breakdown.urgencyClassification / totalCost) * 100 : 0;
                urgencyBar.style.width = percentage + '%';
            }

            // Actualizar Asistente IA (renombrar el tercer elemento)
            const aiAmount = breakdownItems[2].querySelector('.breakdown-amount');
            const aiBar = breakdownItems[2].querySelector('.breakdown-fill');
            const aiService = breakdownItems[2].querySelector('.breakdown-service');
            if (aiAmount && aiBar && aiService) {
                aiService.textContent = 'Asistente Autónomo con IA';
                aiAmount.textContent = formatCurrency(breakdown.aiAssistant);
                const totalCost = Object.values(breakdown).reduce((a, b) => a + b, 0);
                const percentage = totalCost > 0 ? (breakdown.aiAssistant / totalCost) * 100 : 0;
                aiBar.style.width = percentage + '%';
            }
        }
    }

    // Función para actualizar barra de progreso según servicios activos
    function updateResponseProgress(activeServices) {
        const progressBar = document.getElementById('progressBar');
        const progressPercentage = document.getElementById('responsePercentage');
        
        if (progressBar && progressPercentage) {
            // Más servicios = mayor eficiencia (simulado)
            const baseEfficiency = 75;
            const serviceBonus = activeServices * 8;
            const randomVariation = Math.floor(Math.random() * 6) - 3;
            
            const efficiency = Math.min(98, Math.max(60, baseEfficiency + serviceBonus + randomVariation));
            
            progressPercentage.textContent = efficiency + '%';
            progressBar.style.width = efficiency + '%';
        }
    }

    // Event listeners para todos los controles
    messageCountInput.addEventListener('input', updateSimulator);
    sentimentCheckbox.addEventListener('change', updateSimulator);
    urgencyCheckbox.addEventListener('change', updateSimulator);
    aiAssistantCheckbox.addEventListener('change', updateSimulator);

    // Animación especial para checkboxes
    [sentimentCheckbox, urgencyCheckbox, aiAssistantCheckbox].forEach(checkbox => {
        checkbox.addEventListener('change', function() {
            const label = this.nextElementSibling;
            if (label) {
                if (this.checked) {
                    label.style.transform = 'scale(1.02)';
                    setTimeout(() => {
                        label.style.transform = 'scale(1)';
                    }, 150);
                }
            }
        });
    });

    // Inicializar con valores por defecto
    updateSimulator();

    // Actualizar cada 5 segundos para simular cambios en tiempo real
    setInterval(() => {
        if (document.querySelector('.consumption-dashboard')) {
            updateSimulator();
        }
    }, 5000);

    // Animación de conteo animado para números grandes
    function animateNumber(element, finalValue, duration = 1000) {
        const startValue = 0;
        const startTime = performance.now();
        
        function updateNumber(currentTime) {
            const elapsed = currentTime - startTime;
            const progress = Math.min(elapsed / duration, 1);
            
            const currentValue = Math.floor(startValue + (finalValue - startValue) * progress);
            element.textContent = currentValue.toLocaleString('es-CL');
            
            if (progress < 1) {
                requestAnimationFrame(updateNumber);
            }
        }
        
        requestAnimationFrame(updateNumber);
    }

    // Aplicar animación inicial a los números
    setTimeout(() => {
        const messageCount = parseInt(messageCountInput.value) || 5000;
        if (conversationsProcessed) {
            animateNumber(conversationsProcessed, Math.floor(messageCount * 0.7));
        }
    }, 500);
});
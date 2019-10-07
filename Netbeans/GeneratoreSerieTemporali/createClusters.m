clear all;
clc;

%********** INIZIO PARAMETRI DI CONFIGURAZIONE **********
serietemporali_1;
numIterazioni = 20; %settare questa variabile col numero di prove di fcm che si desidera fare
options = [2 500 0.00001 1]; %opzioni per fcm
num_cluster = 3; % numero negativo per far decidere allo script il numero migliore di cluster
%********** INIZIO PARAMETRI DI CONFIGURAZIONE **********

%normalizzazione matrice
minS = min(S); % fa il minimo di ogni colonna (le colonne sono i termini) e restituisce un vettore riga contenente tutti i minimi calcolati
normS = (S-minS)./(max(S)-minS); % normalizza la matrice
S = normS'; % si traspone la matrice perchè vogliamo che le righe siano i termini e le colonne gli intervalli temporali

numTermini = size(S, 1);

if(numTermini/10)<2 %vogliamo almeno 2 cluster
    maxCluster=2;
elseif (numTermini/10)<10 % vogliamo cloud che contengano in media non meno di 10 caratteri
    maxCluster = floor(numTermini/10);
else
   maxCluster = 10;  % non vogliamo più di 10 cloud
end

xie_beni_matr = zeros((maxCluster-1), numIterazioni); % 1)

xie_beni_min=-1;
min_cluster = 2;

if num_cluster > 0
    min_cluster=num_cluster;
    maxCluster=num_cluster;
end

for i = 1 : numIterazioni 
    for j = min_cluster : maxCluster
        [centers,U,objFunc] = fcm(S,j,options);
        compattezza = objFunc(size(objFunc, 1))/j; %prendo il risultato dell'ultima iterazione e lo divido per il numero di cluster i
        distanzeCentroidi = ipdm(centers);
        sepV = distanzeCentroidi(find(distanzeCentroidi~=0));
        separazione= min(sepV)^2;
        xie_beni = compattezza/separazione;
        
        xie_beni_matr(j-1,i)=xie_beni;
         
        if(xie_beni < xie_beni_min || xie_beni_min==-1) %approccio best case!
            xie_beni_min = xie_beni;
            %aggiorno la matrice di appartenenza
            membMatr=U;
            best_num_clusters=j;
        end
    end
end

nclust = zeros(1, (maxCluster-1));
for j = 1 : (maxCluster-1)
    nclust(j)= j+1;
end

%plot(nclust, xie_beni_matr,'-o');
%set(gca, 'YScale', 'log');
%xlabel('Number of Clusters');
%ylabel('Xie-Beni index value');

Y = cmdscale(distanzeCentroidi,2); %funzione usata per ottenere le coordinate dei Centroidi e plottarli in un piano a 2 dimensioni

[grado,I]=max(membMatr);
dlmwrite('resultClustering.txt', I);
dlmwrite('numCluster.txt', best_num_clusters);
dlmwrite('coordinateCentroidi.txt', Y);

%{
1)  Questa matice conterrà tutti gli indici di xie-beni calcolati: in
    particolare: 
    La cella di riga i e colonna j conterrà l'indice di xie-beni ottenuto
    alla prova (iterazione) J-esima con un numero di cl'uster uguale a "i"
%}